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
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

import org.jboss.shotoku.Directory;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.NameFormatException;
import org.jboss.shotoku.svn.SvnContentManager.ExistsType;
import org.jboss.shotoku.svn.operations.AddDirectoryOperation;
import org.jboss.shotoku.svn.operations.ResourceOperation;
import org.jboss.shotoku.tools.Tools;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class SvnMemDirectory extends AbstractSvnDirectory {
	/*
	 * INITIALIZATION AND HELPER METHODS
	 */
	
	private String newPath;
	private String basePath;
	private boolean switchable;
	
	public SvnMemDirectory(String id, String basePath, String newPath, 
			SvnContentManager svnCm) {
		super(id, Tools.concatenatePaths(basePath, newPath), svnCm);
		
		this.basePath = Tools.normalizeSlashes(basePath, true);
		
		// We don't want a / in the beginning, that's why we take a substring.
		this.newPath = Tools.normalizeSlashes(newPath, true).substring(1);
		
		switchable = false;
	}

	@Override
	public void addOperations(Collection<ResourceOperation> ops,
                           SvnContentManager.ContentManagerHolder cmh) {
		if (hasModifications()) {
			// Adding operations for creating new directories and a file.
			String[] newPathParts = newPath.split("[/]");
			String currentPath = basePath;
			/*
			 * This is used to check if we have to check existence of created
			 * directories. Once we have encountered first non-existing
			 * directory, we don't have to check the furhter ones too.
			 */
			boolean checkExistence = true;
            for (String newPathPart : newPathParts) {
                String concatenatedPath = Tools.concatenatePaths(currentPath,
                        newPathPart);
                if (!checkExistence ||
                        getSvnCm().checkIfExists(concatenatedPath) ==
                                ExistsType.DOES_NOT_EXIST) {
                    checkExistence = false;

                    ops.add(new AddDirectoryOperation(getId(), currentPath,
                            newPathPart, concatenatedPath));
                }

                currentPath = Tools.concatenatePaths(currentPath,
                        newPathPart);
            }
			
			// And adding all operations modifying content.
			super.addOperations(ops, cmh);
		}
	}
	
	@Override
	protected String getPropertyInternal(String name) {
		// This property wasn't modfied - so it has no value.
		return null;
	}

	@Override
	protected Map<String, String> getPropertiesInternal() {
		// This resource isn't saved - so it has no properties.
		return new HashMap<String, String>();
	}
	
	@Override
	protected String getLogMessageInternal() {
		// This directory wasn't saved yet.
		return null;
	}

	@Override
	protected String[] listChildNodes() {
		// This directory wasn't saved yet - no child nodes.
		return new String[0];
	}

	@Override
	protected String[] listChildDirectories() {
		// This directory wasn't saved yet - no child directories.
		return new String[0];
	}
	
	@Override
	public boolean switchable() {
		return switchable;
	}
	
	@Override
	public void notifySaved() {
		switchable = true;
	}
	
	@Override
	public boolean forceSwitch() {
		/* 
		 * If a mem resource is switchable, it must have been saved. So,
		 * it must be switched on the nearest occasion.
		 */
		return switchable;
	}
	
	/*
	 * Directory IMPLEMENTATION
	 */

    @Override
	public boolean hasModifications() {
		return true;
	}

    @Override
	public void delete() {
		// We don't have to do anything if this is a new directory, not yet
		// saved.
	}

	@Override
	public SvnNode newNode(String name) throws ResourceAlreadyExists, NameFormatException {
        getSvnCm().checkNotNull(name);

        return newNode(basePath, Tools.concatenatePaths(newPath, name));
    }

	@Override
	public SvnDirectory newDirectory(String name)
            throws ResourceAlreadyExists, NameFormatException {
        getSvnCm().checkNotNull(name);

        return newDirectory(basePath, Tools.concatenatePaths(newPath, name));
    }
	
	@Override
	public void moveTo(Directory dir, String logMessage) {
		throw new RepositoryException("Resource not yet saved!");
	}
	
	@Override
	public void copyTo(Directory dir, String newName, String logMessage) {
		throw new RepositoryException("Resource not yet saved!");
	}
	
	@Override
	public void setIndex(String propertyName, boolean index) {
		throw new RepositoryException("Directory not yet saved.");
	}
	
	@Override
	public boolean hasIndex(String propertyName) {
		return false;
	}

    public long getLastModification() {
		// See SvnMemNode.getLastModification();
        return Calendar.getInstance().getTimeInMillis();
	}
}
