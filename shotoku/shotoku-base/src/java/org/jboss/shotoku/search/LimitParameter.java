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

import java.util.Map;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.NodeList;

/**
 * A search parameter which truncates the number of nodes in the result
 * to the given parameter.
 * @author Adam Warski (adamw@aster.pl)
 */
public class LimitParameter implements SearchParameter {
	private int limit;
	
	public LimitParameter(int limit) {
		this.limit = limit;
	}
	
	public LimitParameter(Map<String, String> params) {
		limit = Integer.valueOf(params.get("limit"));
	}

	public NodeList transform(NodeList list, ContentManager cm) {
		list.limit(limit);
		return list;
	}
}
