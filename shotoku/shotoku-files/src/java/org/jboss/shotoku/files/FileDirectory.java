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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.NodeList;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.exceptions.*;

public class FileDirectory implements Directory {

	private File directory;

	private FileContentManager manager;

	private String dirFullPath;

	private String dirPath;

	private Properties props = new Properties();

	private boolean dirChanged = false;

	private String logMessage;

	private Logger log = Logger.getLogger(this.getClass());

	private static final String CREATE_DATE = "create_date";

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public FileDirectory(FileDirectory parent, String directory,
			FileContentManager manager) throws RepositoryException {

		init(parent.getDirFullPath() + "/" + directory, directory, manager);
	}

	public FileDirectory(String fullPath, String dirName,
			FileContentManager manager) throws RepositoryException {

		init(fullPath, dirName, manager);
	}

	private void init(String fullPath, String directory,
			FileContentManager manager) {
		directory = FileTools.normalizeName(directory);

		dirFullPath = fullPath;

		dirPath = directory;

		this.directory = new File(dirFullPath);

		this.manager = manager;

		if (!this.directory.exists())
			dirChanged = true;

		if (this.directory.exists() && !this.directory.isDirectory()) {
			throw new RepositoryException(directory + " is not a directory");
		}

		refreshLog();
	}

	public String getDirFullPath() {
		return dirFullPath;
	}

	public NodeList getNodes() throws RepositoryException {
		List<Node> list = new ArrayList<Node>();

		File[] children = directory.listFiles();

		// get all children
		for (File file : children) {
			if (file.isFile()) {
				try {
					list
							.add(new FileNode(this, file.getName(), false,
									manager));
				} catch (ResourceAlreadyExists e) {
					// shouldn't happen
					new RepositoryException(e);
				} catch (ResourceDoesNotExist e) {
					// shouldn't happen
					new RepositoryException(e);
				}
			}
		}

		return new NodeList(list);
	}

	public List<Directory> getDirectories() throws RepositoryException {
		List<Directory> list = new ArrayList<Directory>();

		File[] children = directory.listFiles();

		// get all children
		for (File directory : children) {
			if (directory.isDirectory()) {
				list.add(new FileDirectory(this, directory.getName(), manager));
			}
		}

		return list;
	}

	public Node getNode(String name) throws RepositoryException,
			ResourceDoesNotExist {
		name = FileTools.normalizeName(name);

		Directory parent = this;
		String shortName = name;

		if (name.lastIndexOf('/') != -1) {
			parent = getDirectory(name.substring(0, name.lastIndexOf('/')));
			shortName = name.substring(name.lastIndexOf('/'));
		}

		File file = new File(this.directory.getAbsolutePath() + "/" + name);

		if (!file.exists()) {
			throw new ResourceDoesNotExist("File " + file.getAbsolutePath()
					+ " doesn't exists");
		}
		try {
			return new FileNode(parent, shortName, false, manager);
		} catch (ResourceAlreadyExists e) {
			// it's not going to happen
			return null;
		}
	}

	public Directory getDirectory(String name) throws RepositoryException,
			ResourceDoesNotExist {

		name = FileTools.normalizeName(name);

		File dir = new File(this.directory.getAbsolutePath() + "/" + name);

		if (!dir.exists()) {
			throw new ResourceDoesNotExist("Directory " + dir.getAbsolutePath()
					+ " doesn't exists");
		}

		return new FileDirectory(this, name, manager);
	}

	public Node newNode(String name) throws ResourceAlreadyExists,
			RepositoryException, NameFormatException {
		name = FileTools.normalizeName(name);
		Tools.checkName(name);

		File file = new File(this.directory.getAbsolutePath() + "/" + name);

		if (file.exists()) {
			throw new ResourceAlreadyExists("File " + file.getAbsolutePath()
					+ " already exists");
		}

		try {
			return new FileNode(this, name, true, manager);
		} catch (ResourceDoesNotExist e) {
			// not going to happen
			return null;
		}
	}

	public Directory newDirectory(String name) throws ResourceAlreadyExists,
			RepositoryException, NameFormatException {
		Tools.checkName(name.trim());

		File dir = new File(this.directory.getAbsolutePath() + "/" + name);

		if (dir.exists()) {
			throw new ResourceAlreadyExists("Directory "
					+ dir.getAbsolutePath() + " already exists");
		}

		return new FileDirectory(this, name, manager);
	}

	public boolean hasIndex(String arg0) throws RepositoryException {
		// this feature is still not implemented in Shotoku
		return false;
	}

	public void setIndex(String arg0, boolean arg1) throws RepositoryException {
		// this feature is still not implemented in Shotoku

	}

	public void copyTo(Directory dir, String newName, String logMessage)
			throws CopyException {
		try {
			// create new directory
			Directory newDir;
			try {
				newDir = dir.newDirectory(newName);
			} catch (NameFormatException e) {
				throw new CopyException(e);
			}
			List<Directory> dirs = getDirectories();

			// copy all subdirectories
			for (Directory subDir : dirs) {
				subDir.copyTo(newDir, subDir.getName(), logMessage);
			}

			NodeList nodes = getNodes();

			// copy all nodes inside directory
			for (Node node : nodes.toList()) {
				node.copyTo(newDir, node.getName(), logMessage);
			}

		} catch (RepositoryException e) {
			throw new CopyException(e);
		} catch (ResourceAlreadyExists e) {
			throw new CopyException(e);
		}

	}

	public void moveTo(Directory dir, String logMessage) throws MoveException {

		if (dir.getFullName().startsWith(this.getFullName())
				&& dir.getFullName().length() > this.getFullName().length()) {
			throw new MoveException("Can't move to it's child - from: "
					+ this.getFullName() + " to " + dir.getFullName());
		}

		try {
			// create new directory
			Directory newDir = dir.newDirectory(getName());

			// set it's properties
			refreshProps();

			for (Object key : props.keySet()) {
				newDir.setProperty((String) key, props
						.getProperty((String) key));
			}

			// save dir with props
			newDir.save(logMessage);

			List<Directory> dirs = getDirectories();

			// move all subdirectories
			for (Directory subDir : dirs) {
				subDir.moveTo(newDir, logMessage);
			}

			NodeList nodes = getNodes();

			// move all nodes inside directory
			for (Node node : nodes.toList()) {
				node.moveTo(newDir, logMessage);
			}

			// finally delete this dir
			delete();

		} catch (Exception e) {
			throw new MoveException(e);
		}
	}

	public Map<String, String> getProperties() throws RepositoryException {
		try {
			refreshProps();
		} catch (IOException e) {
			throw new RepositoryException(e);
		}

		Map<String, String> map = new HashMap<String, String>();

		for (Object key : props.keySet()) {
			map.put((String) key, props.getProperty((String) key));
		}

		return map;
	}

	private void refreshProps() throws IOException {
		if (!dirChanged) {
			props = FileTools.getPropertiesForDirectory(this, manager);
		}
	}

	public String getProperty(String name) throws RepositoryException {
		try {
			refreshProps();
		} catch (IOException e) {
			throw new RepositoryException(e);
		}

		return props.getProperty(name);
	}

	public void deleteProperty(String name) throws RepositoryException {
		try {
			refreshProps();
		} catch (IOException e) {
			throw new RepositoryException(e);
		}

		dirChanged = true;

		props.remove(name);
	}

	public void setProperty(String name, String value) {
		try {
			refreshProps();
		} catch (IOException e) {
			throw new RepositoryException(e);
		}

		dirChanged = true;

		props.setProperty(name, value);
	}

	public void save(String logMessage) throws SaveException,
			RepositoryException {
		if (!directory.exists())
			if (!directory.mkdirs()) {
				throw new SaveException("Couldn't create directory: "
						+ getFullName());
			}

		if (dirChanged) {
			try {
				File propFile = FileTools.getPropertiesFileForDirectory(this,
						manager);

				File logFile = FileTools.getLogFileForDirectory(this, manager,
						true);

				Properties props = new Properties();

				int i = 1;

				// load message props
				FileInputStream fis = new FileInputStream(logFile);
				props.load(fis);
				fis.close();

				while (props.getProperty(i + ".ver") != null) {
					i++;
				}

				props.setProperty(i + ".ver", logMessage);

				// save message props
				FileOutputStream fos = new FileOutputStream(logFile);
				props.store(fos, FileTools.STORE_TEXT);
				fos.close();

				OutputStream os = new FileOutputStream(propFile);

				props.store(os, FileTools.STORE_TEXT);

				os.close();

				dirChanged = false;
			} catch (IOException e) {
				throw new SaveException(e);
			}
		}

		this.logMessage = logMessage;

	}

	public String getLogMessage() throws RepositoryException {
		if (dirChanged) {
			return null;
		} else {
			refreshLog();
		}

		return logMessage;
	}

	private void refreshLog() {
		try {
			File logFile = FileTools.getLogFileForDirectory(this, manager,
					false);

			if (logFile.exists()) {
				Properties props = new Properties();

				int i = 1;

				// load message props
				FileInputStream fis = new FileInputStream(logFile);
				props.load(fis);
				fis.close();

				while (props.getProperty(i + ".ver") != null) {
					i++;
				}

				logMessage = props.getProperty((i - 1) + ".ver");
			} else {
				logMessage = null;
			}
		} catch (FileNotFoundException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	public String getFullName() {
		String repoPath = manager.getPathToRepoCurrent();
		String dir = directory.getAbsolutePath();

		if (!repoPath.endsWith("/"))
			repoPath = repoPath + "/";

		if ((dir + "/").equals(repoPath)) {
			// root directory
			return "";
		}

		return directory.getAbsolutePath().replace(repoPath, "");
	}

	public Directory getParent() throws RepositoryException {
		return new FileDirectory(directory.getParent(), directory
				.getParentFile().getName(), manager);
	}

	public void delete() throws DeleteException, RepositoryException {

		FileTools.deleteDirectoryRecurseivly(directory, dirFullPath);

		File history = FileTools.getHistoryDir(this, manager, true);

		FileTools.deleteDirectoryRecurseivly(history, dirFullPath);

		history = FileTools.getHistoryDirForSubdirs(this, manager);

		if (history.exists())
			FileTools.deleteDirectoryRecurseivly(history, dirFullPath);

	}

	public String getName() {
		if (directory.getAbsolutePath().equals(manager.getPathToRepoCurrent())) {
			return "";
		}

		return directory.getName();
	}

	public String toString() {
		return directory.getAbsolutePath();
	}

	public long getLastModification() throws RepositoryException {
		return directory.lastModified();
	}

	public Date getLastModificationDate() throws RepositoryException {
		return new Date(getLastModification());
	}

	public boolean hasModifications() {
		// TODO check
		return dirChanged;
	}    

    public ContentManager getContentManager() {
        return manager;
    }
}
