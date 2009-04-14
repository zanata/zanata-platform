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

import java.util.Stack;

import org.jboss.shotoku.tools.Tools;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.ISVNEditor;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class PathsStack {
	private Stack<String> pathsStack;
	private boolean fileOnTop;
	private ISVNEditor editor;
	
	public PathsStack(ISVNEditor editor) throws SVNException {
		pathsStack = new Stack<String>();
		fileOnTop = false;
		this.editor = editor;
		
		editor.openRoot(-1);
	}
	
	/**
	 * Accomodates the paths opened in editor to the given new path. This
	 * should be used only to open existing directories.
	 * @param newPath
	 * @param openLast
     * @param lastRevision
	 * @throws SVNException
	 */
	public void accomodate(String newPath, boolean openLast, long lastRevision)
	throws SVNException {		
		// If there's a file opened, always closing it.
		if (fileOnTop) {
			// Checking if we don't want to have the same file opened.
			if (newPath.equals(pathsStack.peek()))
				return;
			
			editor.closeFile(pathsStack.pop(), null);
			fileOnTop = false;
		}
		
		// Poping and closing all opened directories which
		// don't fit into the new path.
		while ((!pathsStack.empty()) && (!newPath.startsWith(
				pathsStack.peek()))) {
			pathsStack.pop();
			editor.closeDir();
		}
		
		// Opening directories from the new path and putting their paths on the
		// stack.
		String currentPath = pathsStack.empty() ? "/" : pathsStack.peek();
		String[] parts = newPath.substring(currentPath.length()).split("[/]");
		for (int i=0; i < (openLast ? parts.length : parts.length - 1); i++) {
			/*
			 * A path part may be empty in two cases:
			 * 1. pathStack.peek() is equal to newPath
			 * 2. newPath = properPath + "/"
			 * In both cases we don't want to open the "empty" directory.
			 */ 
			if (!"".equals(parts[i])) {
				currentPath += Tools.addPaths(parts[i], "");
				editor.openDir(currentPath, lastRevision);
				pathsStack.push(currentPath);
			}
		}
	}
	
	public void addPath(String path, boolean file) {
		pathsStack.push(file ? path : Tools.addPaths(path, ""));
		fileOnTop = file;
	}
	
	public boolean isFileOpened() {
		return fileOnTop;
	}
	
	public ISVNEditor getEditor() {
		return editor;
	}
	
	public void close() throws SVNException {
		if (isFileOpened()) {
			editor.closeFile(pathsStack.pop(), null);
		}
		
		while (!pathsStack.empty()) {
			editor.closeDir();
			pathsStack.pop();
		}
		
		editor.closeDir();
		editor.closeEdit();
	}
}
