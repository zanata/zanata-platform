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

import java.util.Comparator;
import java.util.Map;

import org.jboss.shotoku.Node;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class PropertySortParameter extends SortParameter {	
	public PropertySortParameter(Map<String, String> params) {
		this(params.get("name"), "true".equals(params.get("descending")));
	}
	
	public PropertySortParameter(final String propertyName, final boolean descending) {
		super (new Comparator<Node>() {
			public int compare(Node o1, Node o2) {
				String prop1 = o1.getProperty(propertyName);
				String prop2 = o2.getProperty(propertyName);
				
				if (prop1 == null) {
					if (prop2 == null) {
						return 0;
					} else {
						return descending ? 1 : -1;
					}
				}
				
				return descending ?
						 prop1.compareTo(prop2) : prop2.compareTo(prop1);
			}		
		});
	}
}