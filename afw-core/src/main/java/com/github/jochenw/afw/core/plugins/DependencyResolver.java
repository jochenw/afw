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

public class DependencyResolver {
	public static class Node<T extends Object> {
		private final String id;
		private final List<String> dependsOn;
		private final T object;
		public Node(String pId, List<String> pDependsOn, T pObject) {
			id = pId;
			dependsOn = pDependsOn;
			object = pObject;
		}
		public Node(String pId, List<String> pDependsOn) {
			this(pId, pDependsOn, null);
		}
		public String getId() {
			return id;
		}
		public List<String> getDependsOn() {
			return dependsOn;
		}
		public T getObject() {
			return object;
		}
	}

	private static class CountedNode<T extends Object> {
		private final Node<T> node;
		private int numberOfReferences;

		CountedNode(Node<T> pNode) {
			node = pNode;
		}
	}

	public static class DuplicateNodeIdException extends RuntimeException {
		private static final long serialVersionUID = -7512617047451839575L;
		private final String id;
		private final Node<?> node0, node1;

		public DuplicateNodeIdException(String pId, Node<?> pNode0, Node<?> pNode1) {
			id = pId;
			node0 = pNode0;
			node1 = pNode1;
		}

		public String getId() {
			return id;
		}

		public Node<?> getNode0() {
			return node0;
		}

		public Node<?> getNode1() {
			return node1;
		}
	}
	public static class UnknownNodeIdException extends RuntimeException {
		private static final long serialVersionUID = -5541605129065404997L;
		private final String id;
		private final Node<?> node;

		public UnknownNodeIdException(String pId, Node<?> pNode) {
			id = pId;
			node = pNode;
		}

		public String getId() {
			return id;
		}

		public Node<?> getNode() {
			return node;
		}
	}
	public static class CircularDependencyException extends RuntimeException {
		private static final long serialVersionUID = 1154298234609885033L;
		private final List<String> ids;

		public CircularDependencyException(List<String> pIds) {
			this.ids = pIds;
		}

		public List<String> getIds() {
			return ids;
		}
	}

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
