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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class SvnRepoDirectory extends AbstractSvnDirectory {
	/*
	 * INITIALIZATION AND HELPER METHODS
	 */
	
	private Map<String, String> properties;
    private Map<String, String> filteredProperties;
    private String[] childNodes;
	private String[] childDirs;
	private SvnRevisionInfo info;
	
	public SvnRepoDirectory(String id, String fullPath, SvnContentManager svnCm) {
		super(id, fullPath, svnCm);
	}

    private void loadDirectoryInfo() {
		info = SvnTools.getCurrentRevisionInformation(getFullPath(), 
			getSvnCm()).get(0);
	}
	
	private void loadDirectoryContent() {
		properties = new HashMap<String, String>();
		
		List<String> childNodesList = new ArrayList<String>();
		List<String> childDirsList = new ArrayList<String>();
		SvnTools.getDirectoryInfo(getFullPath(), -1,
				getSvnCm(), properties, childNodesList, childDirsList);

        filteredProperties = new HashMap<String, String>(properties);
        SvnTools.filterProperties(filteredProperties);

        childNodes = childNodesList.toArray(new String[0]);
		childDirs = childDirsList.toArray(new String[0]);
	}
	
	/*
	 * INTERNAL OPS IMPLEMENTATION
	 */

    @Override
    protected String[] listChildNodes() {
		loadDirectoryContent();
		return childNodes;
	}

    @Override
    protected String[] listChildDirectories() {
		loadDirectoryContent();
		return childDirs;
	}

    @Override
	protected String getPropertyInternal(String name) {
		loadDirectoryContent();
		return properties.get(name);
	}

	@Override
	protected Map<String, String> getPropertiesInternal() {
		loadDirectoryContent();
		return filteredProperties;
	}

	@Override
	protected String getLogMessageInternal() {
		loadDirectoryInfo();
		return info.getMessage();
	}

    /*
     * Directory IMPLEMENTATION
     */

    public long getLastModification() {
        loadDirectoryInfo();
        return info.getDate().getTime();
    }
}
