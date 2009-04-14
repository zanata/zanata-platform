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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.NodeList;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.NameFormatException;
import org.jboss.shotoku.svn.operations.CopyDirectoryOperation;
import org.jboss.shotoku.svn.operations.ModifyDirectoryOperation;
import org.jboss.shotoku.svn.operations.ResourceOperation;
import org.jboss.shotoku.tools.Tools;
import org.tmatesoft.svn.core.SVNException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public abstract class AbstractSvnDirectory extends AbstractSvnResource implements SvnDirectory {
	/*
     * INITIALIZATION AND HELPER METHODS
     */
	
	public AbstractSvnDirectory(String id, String fullPath, SvnContentManager svnCm) {
		super(id, fullPath, svnCm);
	}

	public void addOperations(Collection<ResourceOperation> ops,
                              SvnContentManager.ContentManagerHolder cmh) {
		if (hasModifications()) {
            ops.add(new ModifyDirectoryOperation(getId(), getFullPath(),
                    getModifiedProperties(), getDeletedProperties()));
		}
	}

    /**
     * Checks that the given resource doesn't exist and has a
     * valid name (otherwise, an appropriate exception is thrown).
     * @param basePath
     * @param newPath
     * @return Full path to the new resource.
     * @throws ResourceAlreadyExists
     */
    private String checkNewResource(String basePath, String newPath)
    throws ResourceAlreadyExists, NameFormatException {
        newPath = Tools.normalizeSlashes(newPath, true);

        Tools.checkName(newPath);

        String fullPath = Tools.concatenatePaths(basePath, newPath);
		if (getSvnCm().checkIfExists(fullPath) !=
			SvnContentManager.ExistsType.DOES_NOT_EXIST) {
			throw new ResourceAlreadyExists(newPath);
		}

        return fullPath;
    }

    protected SvnNode newNode(String basePath, String newPath)
            throws ResourceAlreadyExists, NameFormatException {
        return new SvnNodeProxy(getId(), checkNewResource(basePath, newPath),
                getSvnCm(), new SvnMemNode(getId(), basePath, newPath, getSvnCm()));
    }

	protected SvnDirectory newDirectory(String basePath, String newPath)
            throws ResourceAlreadyExists, NameFormatException {
        return new SvnDirectoryProxy(getId(), checkNewResource(basePath, newPath),
                getSvnCm(), new SvnMemDirectory(getId(), basePath, newPath, getSvnCm()));
    }
	
	protected ResourceOperation getCopyOperation(SvnResource 
			destination, String newName) throws SVNException {
		
		return new CopyDirectoryOperation(getId(), getFullPath(), 
				destination.getFullPath(), newName, 
				getCopyRevision());
	}
	
	/*
	 * INTERNAL OPS DECLARATION
	 */
	
	protected abstract String[] listChildNodes();
	protected abstract String[] listChildDirectories();
	
	/*
	 * Directory IMPLEMENTATION
	 */
	
	public boolean hasIndex(String propertyName) {
		throw new RuntimeException("Operation not yet implemented");
	}

	public void setIndex(String propertyName, boolean index) {
		throw new RuntimeException("Operation not yet implemented");
	}
	
	public SvnNode getNode(String name) throws RepositoryException, 
	ResourceDoesNotExist {
		return getSvnCm().getNode(Tools.concatenatePaths(getFullName(), name));
	}

	public SvnDirectory getDirectory(String name) throws RepositoryException, 
	ResourceDoesNotExist {
		return getSvnCm().getDirectory(Tools.concatenatePaths(getFullName(), 
				name));
	}
	
	public NodeList getNodes() {
		List<Node> ret = new ArrayList<Node>();
		
		String[] children = listChildNodes();
		Arrays.sort(children);
		for (String node : children) {
			ret.add(new SvnNodeProxy(getId(), 
					Tools.concatenatePaths(getFullPath(), node), getSvnCm()));
		}
		
		return new NodeList(ret);
	}

	public List<Directory> getDirectories() {
		List<Directory> ret = new ArrayList<Directory>();
		
		String[] children = listChildDirectories();
		Arrays.sort(children);
		for (String directory : children) {
			ret.add(new SvnDirectoryProxy(getId(), 
					Tools.concatenatePaths(getFullPath(), directory), getSvnCm()));
		}
		
		return ret;
	}
	
	public SvnNode newNode(String name) throws ResourceAlreadyExists, NameFormatException {
        getSvnCm().checkNotNull(name);
        return newNode(getFullPath(), name);
    }

	public SvnDirectory newDirectory(String name)
            throws ResourceAlreadyExists, NameFormatException {
        getSvnCm().checkNotNull(name);
        return newDirectory(getFullPath(), name);
    }
}
