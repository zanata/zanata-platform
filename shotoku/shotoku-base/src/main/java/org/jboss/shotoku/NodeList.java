/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.shotoku;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A class representing a list of nodes and providing the possibility to
 * manipulate these nodes.
 * 
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class NodeList implements Collection<Node> {
	private List<Node> nodeList;

	public NodeList() {
		nodeList = new ArrayList<Node>();
	}

	public NodeList(List<Node> nodeList) {
		this.nodeList = nodeList;
	}

	/**
	 * Adds the given node to the node list.
	 * 
	 * @param node
	 *            Node to add.
	 */
	public boolean add(Node node) {
		nodeList.add(node);
		return true;
	}
	
	/**
	 * Adds nodes from the given node list to this node list.
	 * 
	 * @param list Node list to add.
	 */
	public void addAll(NodeList list) {
		nodeList.addAll(list.toList());
	}
	
	/**
	 * Sorts this node list with the given comparator.
	 * @param comparator Comparator to sort with.
	 */
	public void sort(Comparator<Node> comparator) {
		Collections.sort(nodeList, comparator);
	}
	
	/**
	 * Gets an immutable <code>java.util.List</code> representation of this 
	 * node list.
	 * 
	 * @return An immutable <code>java.util.List</code> representation of 
	 * this node list.
	 */
	public List<Node> toList() {
		return Collections.unmodifiableList(nodeList);
	}
	
	/**
	 * Limits the size of this node list. After this operation,
	 * the size of this node list will be lower or equal to the
	 * given limit.
	 * @param limit Node count limit.
	 */
	public void limit(int limit) {
		if (nodeList.size() > limit) {
			nodeList = nodeList.subList(0, limit);
		}
	}

    /**
     * Gets the i-th element of the list.
     * @param i Index of the element to get.
     * @return Node at the given index.
     */
    public Node get(int i) {
        return nodeList.get(i);
    }

    /*
	 * Implementation of the Collection interface methods.
	 */

	public Iterator<Node> iterator() {
		return toList().iterator();
	}
	
	public int size() {
		return nodeList.size();
	}

	public boolean isEmpty() {
		return nodeList.isEmpty();
	}

	public boolean contains(Object o) {
        //noinspection SuspiciousMethodCalls
        return nodeList.contains(o);
	}

	public Object[] toArray() {
		return nodeList.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return nodeList.toArray(a);
	}

	public boolean remove(Object o) {
        //noinspection SuspiciousMethodCalls
        return nodeList.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return nodeList.containsAll(c);
	}

	public boolean addAll(Collection<? extends Node> c) {
		return nodeList.addAll(c);
	}

	public boolean removeAll(Collection<?> c) {
		return nodeList.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return nodeList.retainAll(c);
	}

	public void clear() {
		nodeList.clear();
	}
}
