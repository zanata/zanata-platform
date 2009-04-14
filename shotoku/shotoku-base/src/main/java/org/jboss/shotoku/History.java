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

import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

/**
 * An interface that should be implemented by classes which represent a node's
 * history.
 * 
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public interface History {
	/**
	 * Gets the number of revisions of this node's history.
	 * 
	 * @return Number of revisions of this node's history.
	 * @throws RepositoryException
	 */
	public int getRevisionsCount() throws RepositoryException;

	/**
	 * Gets a node with a given revision. Revisions are numbered from 0.
	 * 
	 * @param revision
	 *            Revision number to get.
	 * @return A node at the given revision.
	 * @throws ResourceDoesNotExist
	 * @throws RepositoryException
	 */
	public Node getNodeAtRevision(int revision) throws ResourceDoesNotExist,
			RepositoryException;

	/**
	 * Gets a list of all nodes in this history.
	 * 
	 * @return A list of all nodes in this history.
	 * @throws RepositoryException
	 */
	public NodeList getAllRevisions() throws RepositoryException;
}
