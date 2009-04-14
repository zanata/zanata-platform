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

import java.io.File;
import java.util.Map;

import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.common.content.FileNodeContent;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class SvnFileNode extends AbstractSvnNode {
	/*
	 * INITIALIZATION AND HELPER METHODS
	 */
	
	private File file;
	
	public SvnFileNode(String id, String fullPath, SvnContentManager svnCm) {
		super(id, fullPath, svnCm);
		
		file = svnCm.getFileForPath(getFullPath());
		setNodeContent(new FileNodeContent(file));
	}

	@Override
	protected String getPropertyInternal(String name) {
		return SvnTools.getProperty(getSvnCm(), file, name);
	}

	@Override
	protected String getLogMessageInternal() {
		return SvnTools.getLogMessageInternal(getSvnCm(), file);
	}
	
	@Override
	protected Map<String, String> getPropertiesInternal() {
		return SvnTools.getAllProperties(file, getSvnCm());
	}
	
	/*
	 * Node IMPLEMENTATION
	 */
	
	public long getLastModification() {
		// TODO if (hasModifications()) {
        if (true) {
            return file.lastModified();
		} else {
			try {
				return getSvnCm().getClientManager().getWCClient().doInfo(file,
						SVNRevision.BASE).getCommittedDate().getTime();
			} catch (SVNException e) {
				throw new RepositoryException(e);
			}
		}
	}
}
