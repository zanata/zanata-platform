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

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.shotoku.tools.Pair;
import org.jboss.shotoku.tools.Tools;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.io.ISVNWorkspaceMediator;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class WorkspaceMediator implements ISVNWorkspaceMediator {
	private Set<String> largePaths;
	
	private Map<Object, Pair<File, OutputStream>> tmpFiles;
	private Map<Object, ByteArrayOutputStream> tmpInMemory;
	
	public WorkspaceMediator(Set<String> largePaths) {
		tmpFiles = new HashMap<Object, Pair<File, OutputStream>>();
		tmpInMemory = new HashMap<Object, ByteArrayOutputStream>();
		
		this.largePaths = largePaths;
	}
	
	public SVNPropertyValue getWorkspaceProperty(String path, String name)
	throws SVNException {
		return null;
	}
	
	public void setWorkspaceProperty(String path, String name, SVNPropertyValue value)
	throws SVNException {
		
	}
	
	public OutputStream createTemporaryLocation(String path, Object id)
	throws SVNException {
		if (largePaths.contains(path)) {
			File tmpFile = Tools.createTemporaryFile();
            OutputStream tmpOs;
            try {
                tmpOs = new BufferedOutputStream(
                        new FileOutputStream(tmpFile));
            } catch (FileNotFoundException e) {
                throw new SVNException(SVNErrorMessage.create(
                        SVNErrorCode.IO_ERROR, e.getMessage(), e));
            }
            tmpFiles.put(id, new Pair<File, OutputStream>(tmpFile, tmpOs));
			
			return tmpOs;
		} else {
			ByteArrayOutputStream tmpBaos = new ByteArrayOutputStream();
			tmpInMemory.put(id, tmpBaos);
			
			return tmpBaos;
		}
	}
	
	public InputStream getTemporaryLocation(Object id) throws SVNException {
		if (tmpFiles.containsKey(id)) {
            try {
                tmpFiles.get(id).getSecond().flush();
                return new BufferedInputStream(new FileInputStream(
                    tmpFiles.get(id).getFirst()));
            } catch (IOException e) {
                throw new SVNException(SVNErrorMessage.create(
                        SVNErrorCode.IO_ERROR, e.getMessage(), e));
            }
		} else {
			return new ByteArrayInputStream(tmpInMemory.get(id).toByteArray());
		}
	}
	
	public long getLength(Object id) throws SVNException {
		if (tmpFiles.containsKey(id)) {
            try {
                tmpFiles.get(id).getSecond().flush();
            } catch (IOException e) {
                throw new SVNException(SVNErrorMessage.create(
                        SVNErrorCode.IO_ERROR, e.getMessage(), e));
            }
            return tmpFiles.get(id).getFirst().length();
		} else {		
			ByteArrayOutputStream tmpBaos = tmpInMemory.get(id);
			if (tmpBaos != null) {
				return tmpBaos.size();
			}
		
			return 0;
		}
	}
	
	public void deleteTemporaryLocation(Object id) {
		if (tmpFiles.containsKey(id)) {
			tmpFiles.get(id).getFirst().delete();
			tmpFiles.remove(id);
		} else {
			tmpInMemory.remove(id);
		}
	}

}
