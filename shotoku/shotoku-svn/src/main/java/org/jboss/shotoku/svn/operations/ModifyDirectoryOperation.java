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

import java.util.Map;
import java.util.Set;

import org.jboss.shotoku.svn.SvnService;
import org.jboss.shotoku.tools.Tools;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.ISVNEditor;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class ModifyDirectoryOperation  extends ResourceOperation {
	private Map<String, String> properties;
	private Set<String> deletedProperties;

    public ModifyDirectoryOperation(String id, String path,
			Map<String, String> properties, Set<String> deletedProperties) {
		super(id, path, OpCode.MODIFY_DIRECTORY);
		
		this.properties = properties;
		this.deletedProperties = deletedProperties;
    }
	
	public void execute(PathsStack stack, long lastRevision) throws SVNException {
		stack.accomodate(Tools.addPaths(path, ""), true, lastRevision);
		
		ISVNEditor editor = stack.getEditor();

		for (String key : properties.keySet()) {
			editor.changeDirProperty(key, properties.get(key));
		}
		
		for (String key : deletedProperties) {
			editor.changeDirProperty(key, null);
		}
	}

	public void addModifiedPaths(SvnService service) {
		service.addDirectoryToModfied(id, path);
    }
}