/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/** The {@link DependencyResolver} works on a graph of nodes, where some of
 * the nodes have dependencies on others, with no circular dependencies. The
 * resolver has the ability, to convert the graph into a list of nodes with
 * the following properties:
 * <ol>
 *   <li>The list is complete: It contains all the nodes from the graph.</li>
 *   <li>If a node in the graph has a dependency on another node, then the
 *     latter precedes the former in the list.</li>
 * </ol>
 */
public class DependencyResolver {
	/** Representation of a node in the graph.
	 * @param <T> The node type.
	 */
	public static class Node<T extends Object> {
		private final String id;
		private final List<String> dependsOn;
		private final T object;
		/** Creates a new instance with an object reference.
		 * @param pId The nodes id.
		 * @param pDependsOn List of the node id's, that the created node depends on.
		 * @param pObject The object, that is referenced by the node.
		 */
		public Node(String pId, List<String> pDependsOn, T pObject) {
			id = pId;
			dependsOn = pDependsOn;
			object = pObject;
		}
		/** Creates a new instance without object reference.
		 * @param pId The nodes id.
		 * @param pDependsOn List of the node id's, that the created node depends on.
		 */
		public Node(String pId, List<String> pDependsOn) {
			this(pId, pDependsOn, null);
		}
		/**
		 * Returns the nodes id.
		 * @return The node id.
		 */
		public String getId() {
			return id;
		}
		/**
		 * Returns a list of node id's, that this one depends on.
		 * @return A list of node id's, that this one depends on.
		 */
		public List<String> getDependsOn() {
			return dependsOn;
		}
		/**
		 * Returns the referenced object, if any, or null.
		 * @return The referenced object, if any, or null.
		 */
		public T getObject() {
			return object;
		}
	}

	/** A helper object, which is internally used while resolving the graph.
	 * Basically, a counter of unresolved references.
	 * @param <T> The node type.
	 */
	private static class CountedNode<T extends Object> {
		private final Node<T> node;
		private int numberOfReferences;

		CountedNode(Node<T> pNode) {
			node = pNode;
		}
	}

	/**
	 * An exception, which is thrown, if a duplicate node id is detected.
	 */
	public static class DuplicateNodeIdException extends RuntimeException {
		private static final long serialVersionUID = -7512617047451839575L;
		private final String id;
		private final Node<?> node0, node1;

		/**
		 * Creates a new instance.
		 * @param pId The node id, that has been detected to be duplicate.
		 * @param pNode0 The first node with the id {@code pId}.
		 * @param pNode1 The first node with the id {@code pId}.
		 */
		public DuplicateNodeIdException(String pId, Node<?> pNode0, Node<?> pNode1) {
			id = pId;
			node0 = pNode0;
			node1 = pNode1;
		}

		/**
		 * Returns the node id, that has been detected to be duplicate.
		 * @return The node id, that has been detected to be duplicate.
		 */
		public String getId() {
			return id;
		}

		/** Returns the first node with the id {@code #getId()}.
		 * @return the first node with the id {@code #getId()}.
		 */
		public Node<?> getNode0() {
			return node0;
		}

		/** Returns the second node with the id {@code #getId()}.
		 * @return the second node with the id {@code #getId()}.
		 */
		public Node<?> getNode1() {
			return node1;
		}
	}
	/** An exception, that is thrown, if a node has a dependency on an
	 * unknown id.
	 */
	public static class UnknownNodeIdException extends RuntimeException {
		private static final long serialVersionUID = -5541605129065404997L;
		private final String id;
		private final Node<?> node;

		/** Creates a new instance.
		 * @param pId Id of the missing node.
		 * @param pNode The node, that refers this id in its dependency list.
		 */
		public UnknownNodeIdException(String pId, Node<?> pNode) {
			id = pId;
			node = pNode;
		}

		/** Returns the Id of the missing node.
		 * @return The Id of the missing node.
		 */
		public String getId() {
			return id;
		}

		/** Returns the node, that references the unknown id in its dependsOn list.
		 * @return The node, that refers this id in its dependency list.
		 */
		public Node<?> getNode() {
			return node;
		}
	}
	/**
	 * An exception, that is thrown, if the graph contains a circular
	 * dependency.
	 */
	public static class CircularDependencyException extends RuntimeException {
		private static final long serialVersionUID = 1154298234609885033L;
		private final List<String> ids;

		/**
		 * Creates a new instance.
		 * @param pIds List of the id's, that make up the circle.
		 */
		public CircularDependencyException(List<String> pIds) {
			this.ids = pIds;
		}

		/** Returns the list of the id's, that make up the circle.
		 * @return The list of the id's, that make up the circle.
		 */
		public List<String> getIds() {
			return ids;
		}
	}

	/**
	 * Called to resolve the dependency graph.
	 * @param <T> The node type.
	 * @param pNodes The nodes in the graph, in arbitrary order.
	 * @return The resolved node list.
	 * @throws DuplicateNodeIdException The node graph contains a duplicate node id.
	 * @throws UnknownNodeIdException There is a node in the graph, that depends on
	 *   an unknown node.
	 * @throws CircularDependencyException The graph contains a circular dependency.
	 */
	public <T extends Object> List<Node<T>> resolve(List<Node<T>> pNodes)
		throws DuplicateNodeIdException, UnknownNodeIdException, CircularDependencyException {
		final Map<String,CountedNode<T>> map = new HashMap<>();
		for (Node<T> node : pNodes) {
			final CountedNode<T> cn1 = new CountedNode<T>(node);
			final CountedNode<T> cn0 = map.put(node.getId(), cn1);
			if (cn0 != null) {
				throw new DuplicateNodeIdException(node.getId(), cn0.node, node);
			}
		}
		for (Node<T> node : pNodes) {
			for (String dep : node.dependsOn) {
				final CountedNode<T> cn = map.get(dep);
				if (cn == null) {
					throw new UnknownNodeIdException(dep, node);
				}
				cn.numberOfReferences++;
			}
		}
		final List<CountedNode<T>> nodes = new ArrayList<>(map.values());
		final List<CountedNode<T>> resolvedNodes = new ArrayList<>(nodes.size());
		while (!nodes.isEmpty()) {
			CountedNode<T> cn = null;
			for (Iterator<CountedNode<T>> iter = nodes.iterator();
					iter.hasNext();  ) {
				final CountedNode<T> cNode = iter.next();
				if (cNode.numberOfReferences == 0) {
					iter.remove();
					cn = cNode;
					break;
				}
			}
			if (cn == null) {
				final Function<CountedNode<T>, String> mapper = (n) -> n.node.getId();
				final List<String> idList = nodes.stream().map(mapper).collect(Collectors.toList());
				throw new CircularDependencyException(idList);
			} else {
				for (String dep : cn.node.dependsOn) {
					map.get(dep).numberOfReferences--;
				}
				resolvedNodes.add(cn);
			}
		}
		final List<Node<T>> result = new ArrayList<>(resolvedNodes.size());
		for (int i = resolvedNodes.size()-1;  i >= 0;  i--) {
			result.add(resolvedNodes.get(i).node);
		}
		return result;
	}
}
