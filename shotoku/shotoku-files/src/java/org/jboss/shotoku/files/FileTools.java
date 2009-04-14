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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.activation.MimetypesFileTypeMap;

import org.apache.log4j.Logger;
import org.jboss.shotoku.exceptions.DeleteException;
import org.jboss.shotoku.exceptions.RepositoryException;

public class FileTools {

	private static MimetypesFileTypeMap mimeTypes;

	private final static String HIST_PROPS = "history.properties";

	private final static String PROPS = "properties.properties";
	
	private final static String LOG = "log.properties";

	public static final String STORE_TEXT = "Properties saved by Shotoku file-based";

	public static final String REVISION_SUFFIX = ".version";

	public static final String PROPERTIES_SUFFIX = ".properties";

	private static Logger log = Logger.getLogger(FileTools.class);
	
	static {
		mimeTypes = new MimetypesFileTypeMap(FileTools.class
				.getResourceAsStream("/mime-types.txt"));
	}

	public static String getMimeType(File file) {
		return mimeTypes.getContentType(file);
	}

	public static File getFileAtRevision(FileNode node, int revison,
			FileContentManager manager) {
		
		String directory = node.getDirectory().getFullName();
		log.debug("directory1: "+directory);
		String pathToHistory = manager.getPathToRepoHistory()
				+ "/" + directory + (directory.length() > 0 ? "/" : "")
				+ node.getNodeName() + "-his" + "/" + revison
				+ REVISION_SUFFIX;

		File hisFile = new File(pathToHistory);

		return hisFile;
	}

	public static File getHistoryDir(FileNode node, FileContentManager manager) {
		
		String directory = node.getDirectory().getFullName();
		log.debug("directory2: "+directory);
		String pathToHistory = manager.getPathToRepoHistory()
				+ "/" + directory + ((directory.length() > 0) ? "/" : "") 
				+ node.getNodeName() + "-his";
		
		File hisFile = new File(pathToHistory);

		return hisFile;
	}

	public static File getHistoryFile(FileNode node, FileContentManager manager, boolean create) {
		
		String directory = node.getDirectory().getFullName();
		log.debug("directory3: "+directory);
		String pathToHistory = manager.getPathToRepoHistory()
				+ "/" + directory + ((directory.length() > 0) ? "/" : "")
				+ node.getNodeName() + "-his";

		if (create)
			createHistoryStructure(pathToHistory);

		File hisFile = new File(pathToHistory + "/" + HIST_PROPS);

		return hisFile;
	}

	public static File getPropsFile(FileNode node, FileContentManager manager) {
		
		String directory = node.getDirectory().getFullName();
		log.debug("directory4: "+directory);
		String pathToProps = manager.getPathToRepoHistory()
				+ "/" + directory + (directory.length() > 0 ? "/" : "")
				+ node.getNodeName() + "-his";

		createHistoryStructure(pathToProps);

		File propsFile = new File(pathToProps + "/" + PROPS);

		return propsFile;
	}

	public static Properties getHistoryForNode(FileNode node,
			FileContentManager manager) throws RepositoryException {

		File hisFile = getHistoryFile(node, manager, false);

		Properties hisProps = new Properties();

		try {
			if (hisFile.exists())
				hisProps.load(new FileInputStream(hisFile));
		} catch (FileNotFoundException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}

		return hisProps;
	}

	public static Properties getPropertiesForNode(FileNode node,
			FileContentManager manager) throws RepositoryException {

		File propsFile = getPropsFile(node, manager);

		Properties props = new Properties();

		try {
			props.load(new FileInputStream(propsFile));
		} catch (FileNotFoundException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}

		return props;
	}

	private static void createHistoryStructure(String hisDirStr)
			throws RepositoryException {
		File hisDir = new File(hisDirStr);
		
		log.debug(hisDir.getAbsolutePath());
		
		if (!hisDir.exists()) {
			// create structure
			hisDir.mkdirs();
			try {
				(new File(hisDirStr + "/" + HIST_PROPS))
						.createNewFile();
				(new File(hisDirStr + "/" + PROPS))
						.createNewFile();
			} catch (IOException e) {
				throw new RepositoryException(e);
			}
		}
	}

	public static File getNextHistoryFile(FileNode node,
			FileContentManager manager, int version) {
		String directory = node.getDirectory().getFullName();
		log.debug("directory5: "+directory);
		String pathToHistory = manager.getPathToRepoHistory()
				+ "/" + directory + (directory.length() > 0 ? "/" : "")
				+ node.getNodeName() + "-his";

		createHistoryStructure(pathToHistory);

		File hisFile = new File(pathToHistory + "/" + (version - 1)
				+ REVISION_SUFFIX);

		return hisFile;
	}

	public static File getNextHistoryPropFile(FileNode node,
			FileContentManager manager, int version) {
		String directory = node.getDirectory().getFullName();
		log.debug("directory6: "+directory);
		String pathToHistory = manager.getPathToRepoHistory()
				+ "/" + directory + (directory.length() > 0 ? "/" : "")
				+ node.getNodeName() + "-his";

		createHistoryStructure(pathToHistory);

		File hisFile = new File(pathToHistory + "/" + (version - 1)
				+ PROPERTIES_SUFFIX);

		return hisFile;
	}

	public static Properties getPropertiesAtRevision(FileNode node,
			FileContentManager manager, int version) throws IOException {
		
		String directory = node.getDirectory().getFullName();
		
		log.debug("directory7: "+directory);
		
		String pathToHistory = manager.getPathToRepoHistory()
				+ "/" + directory + (directory.length() > 0 ? "/" : "")
				+ node.getNodeName() + "-his";

		File hisFile = new File(pathToHistory + "/" + version
				+ PROPERTIES_SUFFIX);

		Properties props = new Properties();

		if (hisFile.exists()) {
			InputStream is = new FileInputStream(hisFile);

			props.load(is);

			is.close();

			return props;
		}

		throw new IOException("Couldn't find history props: "+hisFile.getAbsolutePath());
	}

	public static File getHistoryDir(FileDirectory directory,
			FileContentManager manager, boolean create) {
		File historyDir = new File(manager.getPathToRepoHistory()
				+ "/" + directory.getFullName() + "-his");

		log.debug(historyDir.getAbsolutePath());
		
		if (create && !historyDir.exists()) {
			historyDir.mkdirs();
		}

		return historyDir;
	}
	
	public static File getHistoryDirForSubdirs(FileDirectory directory,
			FileContentManager manager) {
		File historyDir = new File(manager.getPathToRepoHistory()
				+ "/" + directory.getFullName());

		return historyDir;
	}

	public static File getPropertiesFileForDirectory(FileDirectory directory,
			FileContentManager manager) throws IOException {
		File propsFile = new File(manager.getPathToRepoHistory()
				+ "/" + directory.getFullName() + "-his" + "/" + PROPS);
		
		if (!propsFile.exists()) {
			getHistoryDir(directory, manager, true);
			propsFile.createNewFile();
		}
		
		return propsFile;
	}
	
	public static Properties getPropertiesForDirectory(FileDirectory directory,
			FileContentManager manager) throws IOException {
		File propsFile = getPropertiesFileForDirectory(directory, manager);
		
		Properties props = new Properties();
		
		InputStream is = new FileInputStream(propsFile);
		
		props.load(is);
		
		is.close();
		
		return props;
	}

	public static String normalizeName(String name) {
        name = name.trim();
        
        if (name.startsWith("/")) {
			name = name.substring(1);
		}
		
		if (name.endsWith("/")) {
			name = name.substring(0, name.length()-1);
		}
		
		return name;
	}

	public static void deleteDirectoryRecurseivly(File directory, String dirFullPath) throws DeleteException {
		File curDir = directory;
		
		while (directory.exists()) {
			File[] files = curDir.listFiles();

			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					if (!files[i].delete()) {
						throw new DeleteException("Couldn't delete directory: "
								+ dirFullPath);
					}
				}
			}
			// after deleting all files check if directory can be deleted
			if (curDir.listFiles().length == 0) {
				File tmp = curDir;
				curDir = directory.getParentFile();
				if (!tmp.delete()) {
					throw new DeleteException("Couldn't delete directory: "
							+ dirFullPath);
				}
			} else {
				// if not - delete next directory
				curDir = curDir.listFiles()[0];
			}

		}
		
	}

	public static File getLogFileForDirectory(FileDirectory directory, FileContentManager manager, boolean create) throws IOException {
		File propsFile = new File(manager.getPathToRepoHistory()
				+ "/" + directory.getFullName() + "-his" + "/" + LOG);
		
		if (create && !propsFile.exists()) {
			getHistoryDir(directory, manager, true);
			propsFile.createNewFile();
		}
		
		return propsFile;
	}
}
