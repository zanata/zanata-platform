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
import org.jboss.shotoku.tools.Tools;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.ISVNEditor;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class CopyDirectoryOperation extends ResourceOperation {
	private String destParentPath;
	private String destPath;
	private long lastRevision;
	
	public CopyDirectoryOperation(String id, String path, String destParentPath,
			String destName, long lastRevision) {
		super(id, path, OpCode.COPY_DIRECTORY);
		
		this.destParentPath = destParentPath;
		this.lastRevision = lastRevision;
		
		destPath = Tools.addPaths(destParentPath,
				destName);
	}
	
	public void execute(PathsStack stack, long lastRevision) throws SVNException {
		stack.accomodate(Tools.addPaths(destPath, ""), false, lastRevision);
		
		ISVNEditor editor = stack.getEditor();
		// When copying, the second argument can't start with a /.
		editor.addDir(destPath, path.substring(1), this.lastRevision);
		stack.addPath(destPath, false);
	}
	
	public void addModifiedPaths(SvnService service) {
		service.addTreeToModfied(id, destParentPath);
	}
}
