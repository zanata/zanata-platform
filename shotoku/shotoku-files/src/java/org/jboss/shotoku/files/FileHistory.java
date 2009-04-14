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
package org.jboss.shotoku.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.shotoku.History;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.NodeList;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

public class FileHistory implements History {

	private FileNode node;

	private FileContentManager manager;

	public FileHistory(FileNode node, FileContentManager manager) {
		this.node = node;
		this.manager = manager;
	}

	public int getRevisionsCount() throws RepositoryException {
		Properties history = FileTools.getHistoryForNode(node, manager);

		int i = 1;

		while (history.getProperty("ver." + i) != null) {
			i++;
		}

		return --i;
	}

	public Node getNodeAtRevision(int revision) throws ResourceDoesNotExist,
			RepositoryException {

		//user gets revision counting from 0
		revision++;
		
		// get copy of this node
		FileNode newNode = (FileNode)node.getDirectory().getNode(node.getName());

		if (newNode.getRevisionNumber() == revision) {
			return newNode;
		}
		else if (revision > newNode.getRevisionNumber()) {
			throw new ResourceDoesNotExist("There is no node at revision: "+ (revision - 1));
		}
		
		newNode.setRevision(revision);
		
		File revFile = FileTools.getFileAtRevision(node, revision, manager);

		try {
			// get properties for version
			Properties props = FileTools.getPropertiesAtRevision(node, manager, revision);
			
			newNode.setProperties(props);
		} catch (IOException e1) {
			throw new RepositoryException(e1);
		}
		
		if (!revFile.exists()) {
			throw new ResourceDoesNotExist("There is no revision " + revision
					+ " of node " + node.getFullName());
		}
		
		//change node's content
		try {
			InputStream is = new FileInputStream(revFile);
			
			newNode.setContent(is);
			
			newNode.setContentChanged(false);
			
			is.close();
		} catch (FileNotFoundException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
		
		return newNode;
	}

	public NodeList getAllRevisions() throws RepositoryException {
		List<Node> list = new ArrayList<Node>();
		
		int max = getRevisionsCount();
		
		for (int i = 0; i < max; i++) {
			try {
				list.add(getNodeAtRevision(i));
			} catch (ResourceDoesNotExist e) {
				throw new RepositoryException(e);
			}
		}
		
		return new NodeList(list);
	}

}
