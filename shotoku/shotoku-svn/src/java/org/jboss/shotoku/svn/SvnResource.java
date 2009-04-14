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
package org.jboss.shotoku.svn;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jboss.shotoku.Resource;
import org.jboss.shotoku.exceptions.DeleteException;
import org.jboss.shotoku.svn.operations.ResourceOperation;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public interface SvnResource extends Resource {
    /**
	 * To the given collection, adds operations that have to be executed
	 * on this resource to save modification that have been done (this can
	 * be: adding a node/ directory, modifying properties, modifying content).
	 * @param ops
     * @param cmh
     */
	public void addOperations(Collection<ResourceOperation> ops,
                              SvnContentManager.ContentManagerHolder cmh);
	/**
	 * To the given collection, adds an operation which, when executed, will
	 * cause the resource to be deleted.
	 * @param ops
	 * @throws DeleteException
	 */
	public void addDeleteOperation(Collection<ResourceOperation> ops) 
	throws DeleteException ;
	
	/**
	 * Called when this resource has been saved. For example, memory resources
	 * have to "know" that they have been saved, and that they should start 
	 * behaving like an ordinary resource.
	 */
	public void notifySaved();
	/**
	 * @return True iff this this resource's impelementation can be switched
	 * between repository and file, basing on the state of the "dirty sets".
	 * In general, new, not-yet-saved resources aren't switchable. Used by
	 * the proxy classes.
	 */
	public boolean switchable();
	/**
	 * @return True iff the current implementation should be forced to be
	 * switched (for example, a just saved memory node should be switched;
	 * used by the proxy classes).
	 */
	public boolean forceSwitch();
	
	/**
	 * To the given set, adds this resource's path if it's content is large and
	 * should be stored in a temporary file instead of a in-memoery array.
	 * @param toFill
	 */
	public void addLargePaths(Set<String> toFill);
	
	/**
	 * @return Full path to this resource (in the svn repository, with the
	 * prefix).
	 */
	public String getFullPath();
	/**
	 * @return Id of the current content manager.
	 */
	public String getId();
	/**
	 * @return Content manager from which this node was obtained from.
	 */
	public SvnContentManager getSvnCm();
	
	/**
	 * @return A map of properties that have been modified in this resource.
	 */
	public Map<String, String> getModifiedProperties();
	/**
	 * @return A set of property names, which have been deleted in this
	 * resource.
	 */
	public Set<String> getDeletedProperties();
	/**
	 * Sets the modified & deleted properties collections to the given ones.
	 * Used when implemenation of a resource is switched and it is modified
	 * (the modifications must propagate to the new implementation that is
	 * to be used).
	 * @param properties
	 * @param deletedProperties
	 */
	public void setModifiedProperties(Map<String, String> properties,
			Set<String> deletedProperties);
}
