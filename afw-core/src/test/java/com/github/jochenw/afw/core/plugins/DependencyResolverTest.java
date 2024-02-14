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

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.plugins.DependencyResolver.CircularDependencyException;
import com.github.jochenw.afw.core.plugins.DependencyResolver.DuplicateNodeIdException;
import com.github.jochenw.afw.core.plugins.DependencyResolver.Node;
import com.github.jochenw.afw.core.plugins.DependencyResolver.UnknownNodeIdException;
import com.github.jochenw.afw.core.util.Objects;


/** Test for the {@link DependencyResolver}.
 */
public class DependencyResolverTest {
	/** Test case for detecting a duplicate id.
	 */
	@Test
	public void testDuplicateId() {
		final Object object0 = new Object();
		final Object object1 = new Object();
		final Object object2 = new Object();
		final @NonNull List<String> list0 = Objects.requireNonNull(Arrays.asList());
		final Node<Object> node0 = new Node<Object>("a", list0, object0);
		final @NonNull List<String> list1 = Objects.requireNonNull(Arrays.asList("a"));
		final Node<Object> node1 = new Node<Object>("b", list1, object1);
		final Node<Object> node2 = new Node<Object>("a", list0, object2);
		try {
			new DependencyResolver().resolve(Arrays.asList(node0, node1, node2));
			Assert.fail("Expected Exception");
		} catch (DuplicateNodeIdException e) {
			Assert.assertEquals("a", e.getId());
			Assert.assertSame(node0, e.getNode0());
			Assert.assertSame(node2, e.getNode1());
		}
	}

	/** Test case for detecting an unknown id.
	 */
	@Test
	public void testUnknownId() {
		final Object object0 = new Object();
		final Object object1 = new Object();
		final Object object2 = new Object();
		final @NonNull List<String> list0 = Objects.requireNonNull(Arrays.asList());
		final Node<Object> node0 = new Node<Object>("a", list0, object0);
		final @NonNull List<String> list1 = Objects.requireNonNull(Arrays.asList("d"));
		final Node<Object> node1 = new Node<Object>("b", list1, object1);
		final @NonNull List<String> list2 = Objects.requireNonNull(Arrays.asList("b"));
		final Node<Object> node2 = new Node<Object>("c", list2, object2);
		try {
			new DependencyResolver().resolve(Arrays.asList(node0, node1, node2));
			Assert.fail("Expected Exception");
		} catch (UnknownNodeIdException e) {
			Assert.assertEquals("d", e.getId());
			Assert.assertSame(node1, e.getNode());
		}
	}

	@SafeVarargs
	private static final <O> @NonNull List<O> asList(O... pElements) {
		return Objects.requireNonNull(Arrays.asList(pElements));
	}

	/** Test case for detecting a circular dependency.
	 */
	@Test
	public void testCircularDependency() {
		final Object object0 = new Object();
		final Object object1 = new Object();
		final Object object2 = new Object();
		final Node<Object> node0 = new Node<Object>("a", asList("c"), object0);
		final Node<Object> node1 = new Node<Object>("b", asList("a"), object1);
		final Node<Object> node2 = new Node<Object>("c", asList("b"), object2);
		try {
			new DependencyResolver().resolve(Arrays.asList(node0, node1, node2));
			Assert.fail("Expected Exception");
		} catch (CircularDependencyException e) {
			final List<String> ids = e.getIds();
			Assert.assertEquals(3, ids.size());
			Assert.assertTrue(ids.contains("a"));
			Assert.assertTrue(ids.contains("b"));
			Assert.assertTrue(ids.contains("c"));
		}
	}

	/** Test case for a successfully resolved graph.
	 */
	@Test
	public void testSuccess() {
		final Object object0 = new Object();
		final Object object1 = new Object();
		final Object object2 = new Object();
		final Node<Object> node0 = new Node<Object>("a", asList(), object0);
		final Node<Object> node1 = new Node<Object>("b", asList("a"), object1);
		final Node<Object> node2 = new Node<Object>("c", asList("b"), object2);
		final List<Node<Object>> nodes;
		nodes = new DependencyResolver().resolve(Arrays.asList(node0, node1, node2));
		Assert.assertEquals(3, nodes.size());
		assertNode(nodes.get(0), "a", node0.getDependsOn(), object0);
		assertNode(nodes.get(1), "b", node1.getDependsOn(), object1);
		assertNode(nodes.get(2), "c", node2.getDependsOn(), object2);
	}

	/** Test case for a successfully resolved graph on a reversed node list.
	 */
	@Test
	public void testSuccessAfterReverse() {
		final Object object0 = new Object();
		final Object object1 = new Object();
		final Object object2 = new Object();
		final Node<Object> node0 = new Node<Object>("a", asList(), object0);
		final Node<Object> node1 = new Node<Object>("b", asList("a"), object1);
		final Node<Object> node2 = new Node<Object>("c", asList("b"), object2);
		final List<Node<Object>> nodes;
		nodes = new DependencyResolver().resolve(Arrays.asList(node2, node1, node0));
		Assert.assertEquals(3, nodes.size());
		assertNode(nodes.get(0), "a", node0.getDependsOn(), object0);
		assertNode(nodes.get(1), "b", node1.getDependsOn(), object1);
		assertNode(nodes.get(2), "c", node2.getDependsOn(), object2);
	}

	private void assertNode(Node<Object> pNode, String pId, List<String> pDependsOn, Object pObject) {
		Assert.assertNotNull(pNode);
		Assert.assertEquals(pId, pNode.getId());
		Assert.assertSame(pObject, pNode.getObject());
		int dependsOnSize = pDependsOn.size();
		Assert.assertEquals(dependsOnSize, pNode.getDependsOn().size());
		for (int i = 0;  i < dependsOnSize;  i++) {
			Assert.assertEquals(pDependsOn.get(i), pNode.getDependsOn().get(i));
		}
	}
}
