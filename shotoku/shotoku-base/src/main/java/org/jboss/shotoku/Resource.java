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

import java.util.Map;
import java.util.Date;

import org.jboss.shotoku.exceptions.CopyException;
import org.jboss.shotoku.exceptions.DeleteException;
import org.jboss.shotoku.exceptions.MoveException;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.SaveException;

/**
 * Common parts of nodes and directories interfaces.
 * 
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public interface Resource {
	/**
	 * Copies this resource to the given directory. This can't be called if
	 * this resource is new and not yet saved. 
	 * 
	 * @param dir
	 *            Directory to copy this resource to. This can't be a new and
	 *            not saved directory.
	 * @param newName
	 * 			  New name of this resource (in directory dir after copying).
	 * @param logMessage
	 * 			  Log message associated with this resource copy.
	 * @throws CopyException
	 */
	public void copyTo(Directory dir, String newName,
			String logMessage) throws CopyException;
	
	/**
	 * Moves this resource to the given directory. This can't be called if
	 * this resource is new and not yet saved. This resource should not be
	 * used after performing this operation.
	 * 
	 * @param dir
	 *            Directory to move this resource to. This can't be a new and
	 *            not saved directory.
	 * @param logMessage
	 * 			  Log message associated with this resource move.
	 * @throws MoveException
	 */
	public void moveTo(Directory dir, String logMessage) throws MoveException;
	
	/**
	 * Gets a map of all properties associated with this resource.
	 * 
	 * @return A map of properties associated with this resource.
	 * @throws RepositoryException
	 */
	public Map<String, String> getProperties() throws RepositoryException;
	
	/**
	 * Gets the value of the given property.
	 * 
	 * @param propertyName
	 *            Name of the property to get.
	 * @return Value of the given property.
	 * @throws RepositoryException
	 */
	public String getProperty(String propertyName) throws RepositoryException;
	
	/**
	 * Deletes the the given property.
	 * 
	 * @param propertyName
	 *            Name of the property to delete.
	 * @throws RepositoryException
	 */
	public void deleteProperty(String propertyName) throws RepositoryException;

	/**
	 * Sets the value of the given property. Only after saving this change will be
	 * persisted.
	 * 
	 * @param propertyName
	 *            Name of the property to set. It must begin with a character,
	 *            and cannot contain any special characters (so the regexp to which
	 *            a property name must match would be [a-z][a-z0-9]*).
	 * @param propertyValue
	 *            Value of the property to set.
	 */
	public void setProperty(String propertyName, String propertyValue);

	/**
	 * Gets a directory to which this node/ directory belongs.
	 * 
	 * @return A directory to which this node/ directory belongs. Null if this
	 *         directory is already the root directory.
	 */
	public Directory getParent() throws RepositoryException;

	/**
	 * Saves modified properties and possibly content (in case of nodes).
	 * 
	 * @param logMessage
	 *            Log message for saving this node/ directory.
	 * @throws SaveException           
	 * @throws RepositoryException
	 */
	public void save(String logMessage) throws SaveException, RepositoryException;

	/**
	 * Deletes this node or directory (immediately, no <code>save()</code> is
	 * needed). This resource should not be used after performing this 
	 * operation.
	 * 
	 * @throws DeleteException
	 * @throws RepositoryException
	 */
	public void delete() throws DeleteException, RepositoryException;

	/**
	 * Gets the name of this resource (node/ directory).
	 * 
	 * @return Name of this resource (node/ directory). An empty string, if this
	 *         is the root directory.
	 */
	public String getName();
	
	/**
	 * Gets the full name of this resource, that is, path to this resource
	 * relative to the content manager this node was read from.
	 * 
	 * @return Full name of this resource.
	 */
	public String getFullName();

    /**
     * Checks if this resource has any modifications (that is, if any of its
     * properties are changed, or content; new resources will always return
     * "true" before a save).
     * @return True iff this resource has unsaved modifications.
     */
    public boolean hasModifications();

    	/**
	 * Gets the last modification time of this node.
	 *
	 * @return Last modification time of this node.
	 * @throws RepositoryException
	 */
	public long getLastModification() throws RepositoryException;

	/**
	 * Gets the last modification time of this node, represented as a date
	 * object.
	 *
	 * @return Last modification time of this node, represented as a date.
	 * @throws RepositoryException
	 */
	public Date getLastModificationDate() throws RepositoryException;

    /**
     * Gets the content manager from which this resource was obtained from.
     *
     * @return Content manager instance from which this resource was
     * obtained from.
     */
    public ContentManager getContentManager();
}
