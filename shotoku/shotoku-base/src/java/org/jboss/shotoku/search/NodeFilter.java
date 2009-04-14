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
package org.jboss.shotoku.search;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.NodeList;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public abstract class NodeFilter implements SearchParameter {
	/**
	 * Filters the given node.
	 * @param node Node to filter.
	 * @return A node that should be included in the final result or null,
	 * if the nothing is to be included.
	 */
	public abstract Node filterNode(Node node);

	public NodeList transform(NodeList list, ContentManager cm) {
		NodeList newList = new NodeList();
		for (Node n: list) {
			Node newNode = filterNode(n);
			if (newNode != null) newList.add(newNode);
		}
		
		return newList;
	}
}
