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

import org.apache.commons.configuration.Configuration;
import org.jboss.shotoku.svn.service.delayed.DelayedOperation;

import java.util.Set;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public interface SvnService {
    /**
     * Registers a repository in the service. Has an effect only if the
     * repository wasn't earlier registered.
     *
     * @param id
     *            Id of the new repository.
     * @param conf
     *            Configuration of the new repository.
     */
    public void registerRepository(String id, Configuration conf);

    /**
     * Tells the service that a node at the given path was modified and it
     * should be read from the repository, until the WC is updated (adds this
     * path to a "dirty set").
     *
     * @param id
     *            Id of the repository.
     * @param fullPath
     *            Full path to the modified resource.
     */
    public void addNodeToModfied(String id, String fullPath);

    /**
     * Checks if a node was modified and should be read straight from the
     * repository (in other words, if it is in a "dirty set").
     *
     * @param id
     *            Id of the repository.
     * @param fullPath
     *            Full path to the resource to be checked.
     * @return True iff a node at the given path is modified.
     */
    public boolean isNodeModified(String id, String fullPath);

    /**
     * Tells the service that a whole tree at the given path was modified
     * (for example, moved or copied) and it should be read from the
     * repository, until the WC is updated (adds this
     * path to a "dirty set").
     *
     * @param id
     *            Id of the repository.
     * @param fullPath
     *            Full path to the modified tree.
     */
    public void addTreeToModfied(String id, String fullPath);

    /**
     * Checks if a the given path isn't in a modified tree and should be read
     * straight from the repository (in other words, if it is in a
     * "dirty set").
     *
     * @param id
     *            Id of the repository.
     * @param fullPath
     *            Full path to the resource to be checked.
     * @return True iff the tree at the given path is modified.
     */
    public boolean isTreeModified(String id, String fullPath);

    /**
     * Tells the service that a directory at the given path was modified and it
     * should be read from the repository, until the WC is updated (adds this
     * path to a "dirty set").
     *
     * @param id
     *            Id of the repository.
     * @param fullPath
     *            Full path to the modified resource.
     */
    public void addDirectoryToModfied(String id, String fullPath);

    /**
     * Checks if a resource was modified and should be read straight from the
     * repository (in other words, if it is in a "dirty set").
     *
     * @param id
     *            Id of the repository.
     * @param fullPath
     *            Full path to the resource to be checked.
     * @return True iff a resource at the given path is modified.
     */
    public boolean isModified(String id, String fullPath);

    /**
     * Checks if a directory was modified and should be read straight from the
     * repository (in other words, if it is in a "dirty set").
     *
     * @param id
     *            Id of the repository.
     * @param fullPath
     *            Full path to the resource to be checked.
     * @return True iff a directory at the given path is modified.
     */
    public boolean isDirectoryModified(String id, String fullPath);

    /**
     * Tells the service that a resource was deleted in the repository (but that
     * won't be reflected in the WC until the next update).
     *
     * @param id
     *            Id of the repository.
     * @param fullPath
     *            Full path to the deleted resource.
     */
    public void addToDeleted(String id, String fullPath);

    /**
     * Checks if a resource is deleted in the repository, but not yet in the
     * WC, unless <code>isModified(id, fullPath)</code> returns
     * <code>true</code>. In that case, the node exists. So, there should be
     * check if a node is modified before checking if it is deleted.
     *
     * @param id
     *            Id of the repository.
     * @param fullPath
     *            Full path to the resource to check.
     * @return True iff a resource at the given path is deleted.
     */
    public boolean isDeleted(String id, String fullPath);

    /**
     * Sets the first update property - if a repository should be updated
     * on registration.
     *
     * @param firstUpdate New value of the first update property.
     */
    public void setFirstUpdate(boolean firstUpdate);

    /**
     * Checks if a repository will be updated on registration.
     * @return True if a repository will be updated on registration.
     */
    public boolean getFirstUpdate();

    /**
     * Adds a delayed operation that will be executed on the nearest
     * service update.
     * @param id Id of the repository in which this operation is to be
     * executed.
     * @param op Operation to perform.
     */
    public void addDelayedOperation(String id, DelayedOperation op);

    /**
     * Adds the given paths to update queue.
     * @param id Id of the repository for which to add the paths.
     * @param revision Revision in which the paths were changed.
     * @param paths Changed paths to update.
     */
    public void addPathsToUpdate(String id, long revision, Set<String> paths);
    
    public String getServiceInfo();
    
    public void update();
    
    public void create() throws Exception;
    public void start() throws Exception;
    public void stop();
    public void destroy();
}
