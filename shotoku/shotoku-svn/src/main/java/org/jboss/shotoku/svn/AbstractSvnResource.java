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

import java.util.*;

import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Resource;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.exceptions.*;
import org.jboss.shotoku.svn.operations.DeleteOperation;
import org.jboss.shotoku.svn.operations.ResourceOperation;
import org.tmatesoft.svn.core.SVNException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public abstract class AbstractSvnResource implements SvnResource {
	/*
	 * FIELDS
	 */
	
	/**
	 * <code>modifiedProperties</code> - a map of properties that have been
	 * modified for this resource and have to be written upon save.
	 */
	private Map<String, String> modifiedProperties;
	/**
	 * <code>deletedProperties</code> - a set of names of deleted properties.
	 */
	private Set<String> deletedProperties;
	
	private SvnService service;
	
	private String id;
	private String fullPath;
	private String name;
	private String fullName;
	private SvnContentManager svnCm;
	
	public AbstractSvnResource(String id, String resFullPath,
			SvnContentManager svnCm) {
		this.id = id;

		this.svnCm = svnCm;
		
		String[] pathParams = svnCm.getNamesFromPath(resFullPath);
		fullPath = pathParams[0];
		name = pathParams[1];
		fullName = pathParams[2];
		
		modifiedProperties = new HashMap<String, String>();
		deletedProperties = new HashSet<String>();
		service = SvnTools.getService();
	}
	
	/*
	 * FIELD GETTERS
	 */
	
	protected SvnService getService() {
		return service;
	}
	
	/*
	 * SvnResource IMPLEMENTATION
	 */

    public void notifySaved() {
		deletedProperties.clear();
		modifiedProperties.clear();
	}
	
	public String getFullPath() {
		return fullPath;
	}
	
	public SvnContentManager getSvnCm() {
		return svnCm;
	}
	
	public String getId() {
		return id;
	}
	
	public Map<String, String> getModifiedProperties() {
		return modifiedProperties;
	}
	
	public Set<String> getDeletedProperties() {
		return deletedProperties;
	}
	
	public void setModifiedProperties(Map<String, String> properties,
			Set<String> deletedProperties) {
		modifiedProperties = properties;
		this.deletedProperties = deletedProperties;
	}
	
	public boolean switchable() {
		return true;
	}
	
	public boolean forceSwitch() {
		return false;
	}
	
	public void addDeleteOperation(Collection<ResourceOperation> ops) 
	throws DeleteException {
		if (("".equals(getFullName()) || ("/".equals(getFullName())))) {
			throw new DeleteException("Cannot delete the root directory!");
		}
		
		ops.add(new DeleteOperation(getId(), getFullPath(), 
				getParent().getFullPath()));
	}
	
	public void addLargePaths(Set<String> toFill) {
		
	}
	
	/*
	 * INTERNAL OPS DECLARATIONS
	 */
	
	protected abstract String getPropertyInternal(String name);
	protected abstract String getLogMessageInternal();
	protected abstract Map<String, String> getPropertiesInternal();
	protected abstract ResourceOperation getCopyOperation(SvnResource 
			destination, String newName) throws SVNException;
	
	protected long getCopyRevision() throws SVNException {
		return getSvnCm().getRepository().getLatestRevision();
	}
	
	/*
	 * Resource INTERFACE IMPLEMENTATION
	 */
	
	public void save(String logMessage) throws SaveException {
		// Checking if there is anything to save.
		if (!hasModifications()) return;

		Set<Resource> toCommit = new HashSet<Resource>();
		toCommit.add(this);
		getSvnCm().save(logMessage, toCommit);
	}
	
	public String getProperty(String name) {
		// First trying to get the properties from the ones that have been
		// already modified, only if it hasn't been written, getting it
		// from svn.
		if (modifiedProperties.containsKey(name)) {
			return modifiedProperties.get(name);
		}
		
		if (deletedProperties.contains(name)) {
			return null;
		}
		
		return getPropertyInternal(name);
	}
	
	public Map<String, String> getProperties() {
		Map<String, String> ret = getPropertiesInternal();
		
		ret.putAll(modifiedProperties);
		for (String deletedPropertyName : deletedProperties) {
			ret.remove(deletedPropertyName);
		}

		return ret;
	}
	
	public void setProperty(String name, String value) {
		if (value == null) {
			/*
			 * Checking if the user doesn't try to delete the property using
			 * this method.
			 */
			deleteProperty(name);
		} else {
			deletedProperties.remove(name);
			modifiedProperties.put(name, value);
		}
	}

	public void deleteProperty(String name) {
		deletedProperties.add(name);
		modifiedProperties.remove(name);
	}
	
	public String getName() {
		return name;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	/**
	 * Casts the given directory to SvnDirectory, and checks if it isn't
	 * new and not yet saved. If so, or there's a ClassCastException, an
	 * InternalSvnException is thrown.
	 */
	private SvnDirectory getSvnDirectory(Directory dir) 
	throws InternalSvnException {
		/*
		 * This must be a svn directory. Who would be stupid enough to
		 * give a directory from another implementation ...
		 */
		SvnDirectory svnDir;
		try {
			svnDir = (SvnDirectory) dir;
		} catch (ClassCastException e) {
			throw new InternalSvnException("Use svn functions to only with " +
					"svn directories!");
		}
		
		if (!svnDir.switchable()) {
			throw new InternalSvnException("The given directory is new and not " +
					"yet saved!");
		}
		
		return svnDir;
	}
	
	public void copyTo(Directory dir, String newName, String logMessage) 
	throws CopyException {
		SvnDirectory svnDir;
		try {
			svnDir = getSvnDirectory(dir);
		} catch (InternalSvnException e) {
			throw new CopyException(e);
		}
		
		try {
			List<ResourceOperation> opsList = 
				new ArrayList<ResourceOperation>();
			
			opsList.add(getCopyOperation(svnDir, newName));
			
			getSvnCm().performOperations(opsList, logMessage);
		} catch (SVNException e) {
			throw new CopyException(e);
		}
	}
	
	public void moveTo(Directory dir, String logMessage) throws MoveException {
		SvnDirectory svnDir;
		try {
			svnDir = getSvnDirectory(dir);
		} catch (InternalSvnException e) {
			throw new MoveException(e);
		}
		
		if ((dir.getFullName() + "/").startsWith(getFullName() + "/")) {
            throw new MoveException("Cannot move a resource to its child!");
        }
		
		try {
			List<ResourceOperation> opsList = 
				new ArrayList<ResourceOperation>();
			
			// Move = copy + delete.
			opsList.add(getCopyOperation(svnDir, getName()));
			opsList.add(new DeleteOperation(getId(), getFullPath(), 
					getParent().getFullPath()));
			
			getSvnCm().performOperations(opsList, logMessage);
		} catch (SVNException e) {
			throw new MoveException(e);
		}
	}
	
	public void delete() throws DeleteException {
		Set<Resource> toDelete = new HashSet<Resource>();
		toDelete.add(this);
		getSvnCm().delete(toDelete);
	}
	
	public String getLogMessage() {
		if (hasModifications())
			return null;
		else
			return getLogMessageInternal();
	}
	
	public SvnDirectory getParent() {
		try {
			return getSvnCm().getDirectory(SvnTools.getParentFullName(this));
		} catch (ResourceDoesNotExist e) {
			// Impossible (almost, unless the dir was deleted ...)
			return null;
		}
	}

    public boolean hasModifications() {
		return modifiedProperties.size() > 0 || deletedProperties.size() > 0;
	}

    public ContentManager getContentManager() {
        return getSvnCm();
    }
    	
	public Date getLastModificationDate() {
		return new Date(getLastModification());
	}
}
