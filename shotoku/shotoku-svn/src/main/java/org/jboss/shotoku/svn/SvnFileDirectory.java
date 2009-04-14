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

import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.SVNException;
import org.jboss.shotoku.exceptions.RepositoryException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class SvnFileDirectory extends AbstractSvnDirectory {
	/*
     * INITIALIZATION AND HELPER METHODS
     */
	
	private File file;
	
	public SvnFileDirectory(String id, String fullPath, SvnContentManager svnCm) {
		super(id, fullPath, svnCm);
		
		file = svnCm.getFileForPath(getFullPath());
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
	 * INTERNAL OPS IMPLEMENTATION
	 */
	
	protected String[] listChildNodes() {
		return getSvnCm().getFileForPath(getFullPath()).list(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
                            return !new File(dir.getAbsolutePath() +
                                    File.separator + name)
                                    .isDirectory();
                        }
					});	
	}

	protected String[] listChildDirectories() {
		return getSvnCm().getFileForPath(getFullPath()).list(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							if (name.equals(".svn"))
								return false;
                            return new File(dir.getAbsolutePath() + File.separator + name)
                                    .isDirectory();
                        }
					});
	}

    /*
     * Directory IMPLEMENTATION
     */

    public long getLastModification() {
		if (hasModifications()) {
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
