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
import java.util.Date;

import org.jboss.shotoku.Directory;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.exceptions.CopyException;
import org.jboss.shotoku.exceptions.DeleteException;
import org.jboss.shotoku.exceptions.MoveException;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceDeleted;
import org.jboss.shotoku.exceptions.SaveException;
import org.jboss.shotoku.svn.operations.ResourceOperation;

/**
 * A base class for new resource proxies - their function is to tunnel all
 * functions to a "new resource" class implementation if a new resource
 * hasn't been yet saved, and to a "normal resource" class implementation if
 * a new resource has been saved.
 * 
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public abstract class SvnResourceProxy implements SvnResource {
	/*
	 * FIELDS
	 */
	
	protected SvnResource currentResource;
	
	private SvnService service;
	private boolean isRepoResource;
	
	private String id;
	private String fullPath;
	private SvnContentManager svnCm;
	
	/*
	 * INITIALIZATION AND HELPER METHODS
	 */
	
	public SvnResourceProxy(String id, String fullPath, SvnContentManager svnCm) {
		service = SvnContentManager.getService();
		
		this.id = id;
		this.fullPath = fullPath;
		this.svnCm = svnCm;
		
		checkSwitch(true);
	}
	
	public SvnResourceProxy(String id, String fullPath, SvnContentManager svnCm,
			SvnResource initialResource) {
		service = SvnContentManager.getService();
		
		this.id = id;
		this.fullPath = fullPath;
		this.svnCm = svnCm;
		
		currentResource = initialResource;
	}

	protected abstract SvnResource getFileResource();
	protected abstract SvnResource getRepoResource();
	
	protected void switchToFileResource() {
		SvnResource oldResource = currentResource;
		currentResource = getFileResource();
		
		if (oldResource != null) {
			currentResource.setModifiedProperties(
				oldResource.getModifiedProperties(),
				oldResource.getDeletedProperties());
		}
		
		isRepoResource = false;
	}
	
	protected void switchToRepoResource() {
		SvnResource oldResource = currentResource;
		currentResource = getRepoResource();
		
		if (oldResource != null) {
			currentResource.setModifiedProperties(
					oldResource.getModifiedProperties(),
					oldResource.getDeletedProperties());
		}
		
		isRepoResource = true;
	}
	
	/**
	 * Checks if the current resource implementation shouldn't be switched from
	 * file to repo (in case a file was modified by somebody else), or from repo
	 * to file (in case the wc was updated and we no longer need to read from
	 * the repository).
	 * 
	 * @param initial
	 *            True, if switching is to be forced to occur.
	 */
	protected void checkSwitch(boolean initial) {
		if ((currentResource != null) && (!currentResource.switchable())) {
			return;
		}
		
		boolean forceSwitch = false;
		if (currentResource != null) {
			forceSwitch = currentResource.forceSwitch();
		}
		
		if (service.isModified(getId(), fullPath)) {
			if (initial || !isRepoResource || forceSwitch) {
				switchToRepoResource();
			}
			
			return;
		} else {
			if (initial || isRepoResource || forceSwitch) {
				switchToFileResource();
				
				return;
			}
		}
		
		if (service.isDeleted(getId(), fullPath)) {
			throw new ResourceDeleted(fullPath); 
		}
	}
	
	/**
	 * This should be called whenever incorrect results could be obtained if a
	 * resource was modified by some other thread and the change isn't yet
	 * reflected in the wc (or, to see if we can't switch back to the wc).
	 * 
	 * @see SvnResourceProxy#checkSwitch(boolean initial)
	 */
	protected void checkSwitch() {
		checkSwitch(false);
	}
	
	/*
	 * SvnResource IMPLEMENTATION
	 */

	public void addOperations(Collection<ResourceOperation> ops,
                              SvnContentManager.ContentManagerHolder cmh) {
		checkSwitch();
		currentResource.addOperations(ops, cmh);
	}
	
	public String getFullPath() {
		return fullPath;
	}
	
	public String getId() {
		return id;
	}

	public SvnContentManager getSvnCm() {
		return svnCm;
	}
	
	public Map<String, String> getModifiedProperties() {
		return currentResource.getModifiedProperties();
	}
	
	public Set<String> getDeletedProperties() {
		return currentResource.getDeletedProperties();
	}
	
	public void setModifiedProperties(Map<String, String> properties,
			Set<String> deletedProperties) {
		currentResource.setModifiedProperties(properties, deletedProperties);
	}
	
	public void notifySaved() {
		// First notifying about save ..
		currentResource.notifySaved();
		
		// ... and checking if we don't have to switch (in case this was
		// a mem resource).
		checkSwitch();
	}
	
	public boolean switchable() {
		return currentResource.switchable();
	}
	
	public boolean forceSwitch() {
		return currentResource.forceSwitch();
	}
	
	public void addDeleteOperation(Collection<ResourceOperation> ops) 
	throws DeleteException {
		currentResource.addDeleteOperation(ops);
	}
	
	public void addLargePaths(Set<String> toFill) {
		currentResource.addLargePaths(toFill);
	}
	
	/*
	 * Resource IMPLEMENTATION
	 */

    public boolean hasModifications() {
		return currentResource.hasModifications();
	}

    public void save(String logMessage) throws RepositoryException,
	SaveException {
		checkSwitch();
		currentResource.save(logMessage);
	}
	
	public String getFullName() {
		return currentResource.getFullName();
	}

	public void delete() throws DeleteException, RepositoryException {
		checkSwitch();
		currentResource.delete();
	}
	
	public String getName() {
		return currentResource.getName();
	}
	
	public Directory getParent() throws RepositoryException {
		return currentResource.getParent();
	}

	public String getProperty(String name) throws RepositoryException {
		checkSwitch();
		return currentResource.getProperty(name);
	}

	public void setProperty(String name, String value) {
		currentResource.setProperty(name, value);
	}
	
	public void deleteProperty(String name) {
		checkSwitch();
		currentResource.deleteProperty(name);
	}

	public Map<String, String> getProperties() throws RepositoryException {
		checkSwitch();
		return currentResource.getProperties();
	}

	public void moveTo(Directory dir, String logMessage) throws MoveException {
		currentResource.moveTo(dir, logMessage);
	}

	public void copyTo(Directory dir, String newName, String logMessage) 
	throws CopyException {
		currentResource.copyTo(dir, newName, logMessage);
	}

    public Date getLastModificationDate() throws RepositoryException {
		// checkSwitch() is called by calling getLastModification(), which is
		// called from the implementation of this function in SvnNode.
		return currentResource.getLastModificationDate();
	}

	public long getLastModification() throws RepositoryException {
		checkSwitch();
		return currentResource.getLastModification();
	}

    public ContentManager getContentManager() {
        return currentResource.getContentManager();
    }
}
