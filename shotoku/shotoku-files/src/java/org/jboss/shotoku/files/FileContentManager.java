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
package org.jboss.shotoku.files;

import java.io.File;
import java.util.Collection;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.Resource;
import org.jboss.shotoku.exceptions.DeleteException;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.SaveException;

public class FileContentManager extends ContentManager {

	private String pathToRepo;
	
	private String pathToRepoCurrent;
	
	private String pathToRepoHistory;
	
	private final static String CURRENT = "CURRENT";
	
	private final static String HISTORY = "HISTORY"; 
	
	private Logger log = Logger.getLogger(FileContentManager.class);

    private String filePrefix;

    public FileContentManager(String id, String prefix, Configuration conf) throws RepositoryException {
		super(id, prefix);

        filePrefix = prefix;

        // add separator at the end and at the begginig
		if (!filePrefix.startsWith("/")) {
			filePrefix = filePrefix.substring(1);
		}
		
		if (filePrefix.endsWith("/")) {
			filePrefix = filePrefix.substring(0, filePrefix.length() - 1);
		}
		
		pathToRepo = conf.getString("localpath");
		
		if (pathToRepo == null) {
			throw new RepositoryException("localpath is not set in properties");
		}
		else if (!(new File(pathToRepo)).exists() || !(new File(pathToRepo)).isDirectory()){
			throw new RepositoryException(pathToRepo+" doesn't exist or isn't a directory");
		}
		
		// create paths
		
		pathToRepoCurrent = pathToRepo + "/" + CURRENT;
		pathToRepoHistory = pathToRepo + "/" + HISTORY;
		
		// check for current and history existance
		File fileCur = new File(pathToRepoCurrent);
		File fileHis = new File(pathToRepoHistory);
		
		// create CURRENT folder if it doesn't exist
		if (!fileCur.exists()) {
			fileCur.mkdirs();
			log.info("Added CURRENT folder");
		}
		
		// create HISTORY folder if it doesn't exist
		if (!fileHis.exists()) {
			fileHis.mkdirs();
			log.info("Added HISTORY folder");
		}
	}

    String getFilePrefix() {
        return filePrefix;
    }

    @Override
	public Directory getRootDirectory() throws RepositoryException {
		return new FileDirectory(getPathToRepoCurrent(), "", this);
	}

	@Override
	public Node getNode(String nodeName) throws ResourceDoesNotExist,
			RepositoryException {
		return getRootDirectory().getNode(nodeName);
	}

	@Override
	public Directory getDirectory(String dirName) throws ResourceDoesNotExist,
			RepositoryException {
		return getRootDirectory().getDirectory(dirName);
	}

	@Override
	public void save(String message, Collection<Resource> resources)
			throws SaveException, RepositoryException {
		for (Resource resource : resources) {
			resource.save(message);
		}
	}

	@Override
	public void delete(Collection<Resource> resources) throws DeleteException,
			RepositoryException {
		for (Resource resource : resources) {
			resource.delete();
		}
	}

	public String getPathToRepo() {
		return pathToRepo;
	}

	public String getPathToRepoCurrent() {
		return pathToRepoCurrent + getFilePrefix();
	}

	public String getPathToRepoHistory() {
		return pathToRepoHistory + getFilePrefix();
	}
}
