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

import java.util.List;

import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.NameFormatException;

/**
 * Interface that must be implemented by classes which represent directories,
 * that is resources, which can contain other directories and nodes.
 * 
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public interface Directory extends Resource {
    /**
     * Gets a list of nodes that are contained in this directory. The nodes are
     * sorted in alphabetic order.
     *
     * @return A list of nodes that are contained in this directory.
     * @throws RepositoryException
     */
    public NodeList getNodes() throws RepositoryException;

    /**
     * Gets a list of directories that are contained in this directory. The
     * directories are sorted in alphabetic order.
     *
     * @return A list of directories that are contained in this directory.
     * @throws RepositoryException
     */
    public List<Directory> getDirectories() throws RepositoryException;

    /**
     * Gets a node from the given directory with the given name. The name can
     * contain <code>/</code>, which just mean getting the node from some
     * subdirectory.
     *
     * @return A node with the given name from this directory.
     * @throws RepositoryException
     */
    public Node getNode(String name) throws RepositoryException,
            ResourceDoesNotExist;

    /**
     * Gets a node from the given directory with the given name. The name can
     * contain <code>/</code>, which just mean getting the directory from
     * some subdirectory.
     *
     * @return A directory with the given name from this directory.
     * @throws RepositoryException
     */
    public Directory getDirectory(String name) throws RepositoryException,
            ResourceDoesNotExist;

    /**
     * Creates and returns a new node in this directory. Only after saving, this
     * node will be visible by other functions and persisted. As in
     * <code>getNode(String)</code>, the name can contain <code>/</code>.
     * In such a case, new directories will be created if need be.
     *
     * @param name
     *            Name of the new node. It cannot contain spaces.
     * @return A new node with the given name.
     * @throws ResourceAlreadyExists
     * @throws NameFormatException
     * @throws RepositoryException
     */
    public Node newNode(String name) throws ResourceAlreadyExists,
            NameFormatException, RepositoryException;

    /**
     * Creates a new directory in this directory. Only after saving this
     * directory, it will be visible by other functions and persisted. As in
     * <code>getDirectory(String)</code>, the name can contain <code>/</code>.
     * In such a case, new directories will be created if need be.
     *
     * @param name
     *            Name of the directory to create. It cannot contain spaces.
     * @return A new directory with the given name.
     * @throws ResourceAlreadyExists
     * @throws NameFormatException
     * @throws RepositoryException
     */
    public Directory newDirectory(String name) throws ResourceAlreadyExists,
            NameFormatException, RepositoryException;

    /**
     * Checks if an index is created on the given property. UNIMPLEMENTED!.
     *
     * @param propertyName
     *            Name of the property for which to check the index.
     * @return True iff an index is created for the given property.
     * @throws RepositoryException
     */
    public boolean hasIndex(String propertyName) throws RepositoryException;

    /**
     * Creates or deletes an index on a property. The directory must be saved
     * before this operation. UNIMPLEMENTED!.
     *
     * @param propertyName
     *            Name of the property for which to create/ delete an index.
     * @param index
     *            True if and index should be created, false in an index should
     *            be deleted.
     * @throws RepositoryException
     */
    public void setIndex(String propertyName, boolean index)
            throws RepositoryException;
}
