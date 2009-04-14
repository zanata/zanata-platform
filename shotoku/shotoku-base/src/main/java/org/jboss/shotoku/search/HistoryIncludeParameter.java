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
import org.jboss.shotoku.History;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.NodeList;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

/**
 * A search parameter which includes to the result all nodes from the
 * history of a given node.
 * @author Adam Warski (adamw@aster.pl)
 */
public class HistoryIncludeParameter implements SearchParameter {
	private History history;
	private String path;

	public HistoryIncludeParameter(History history) {
		this.history = history;
	}
	
	public HistoryIncludeParameter(Node node) {
		history = node.getHistory();
	}
	
	public HistoryIncludeParameter(Map<String, String> params) {
		this.path = params.get("path");
	}

	public NodeList transform(NodeList list, ContentManager cm) throws ResourceDoesNotExist {
		if (path == null) {
			list.addAll(history.getAllRevisions());
		} else {
			list.addAll(cm.getNode(path).getHistory().getAllRevisions());
		}
		
		return list;
	}
}
