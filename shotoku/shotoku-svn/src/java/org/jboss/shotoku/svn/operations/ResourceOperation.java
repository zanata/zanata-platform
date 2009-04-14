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
package org.jboss.shotoku.svn.operations;

import org.jboss.shotoku.svn.SvnService;
import org.tmatesoft.svn.core.SVNException;

/**
 * An base class for operations on the repository.
 * @author Adam Warski (adamw@aster.pl)
 */
public abstract class ResourceOperation implements Comparable<ResourceOperation> {
	protected enum OpCode {
		ADD_DIRECTORY,
		ADD_FILE,
		MODIFY_FILE,
		MODIFY_DIRECTORY,
		DELETE,
		COPY_FILE,
		COPY_DIRECTORY
	}
	
	protected String path;
	protected OpCode opCode;
	protected String id;
	
	public ResourceOperation(String id, String path, OpCode opCode) {
		this.id = id;
		this.path = path;
		this.opCode = opCode;
	}
	
	public int compareTo(ResourceOperation ro) {
		int pathsCompare = path.compareTo(ro.path);
		if (pathsCompare == 0)
			return opCode.compareTo(ro.opCode);
		
		return pathsCompare;
	}
	
	@Override
	public boolean equals(Object obj) {
		ResourceOperation ro = (ResourceOperation) obj;
		return path.equals(ro.path) && opCode.equals(ro.opCode);
	}

	@Override
	public int hashCode() {
		return path.hashCode() + opCode.hashCode();
	}
	
	/**
	 * Checks if this operation encloses the given one, that is, execution
	 * of this operation has more effects then the given one, so 
	 * <code>op</code> doesn't have to be executed to all. 
	 */
	public boolean encloses(ResourceOperation op) {
		return false;
	}
	
	/**
	 * Executes the specific acitons represented by this object.
	 * 
	 * @param stack
     *            Paths stack which represents the currently open paths, and is
     *            an access point to a ISVNEditor instance.
     * @param lastRevision
     * @throws SVNException
	 */
	public abstract void execute(PathsStack stack, long lastRevision) throws SVNException;
	
	/**
	 * Notifies the service about paths modified by this operation. 
	 * @param service
	 */
	public abstract void addModifiedPaths(SvnService service);
}
