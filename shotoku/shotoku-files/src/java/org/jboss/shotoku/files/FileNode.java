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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.History;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.exceptions.*;

public class FileNode implements Node {

    private File file;

    private File tmpFile;

    private FileContentManager manager;

    private Directory directory;

    private Properties props = new Properties();

    private boolean propsChanged = false;

    private boolean contentChanged = false;

    private int revision;

    private boolean lastVersion = true;

    private Logger log = Logger.getLogger(this.getClass());

    private static final String CREATE_DATE = "create_date";

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FileNode(Directory directory, String node, boolean newFile,
                    FileContentManager manager) throws RepositoryException,
            ResourceAlreadyExists, ResourceDoesNotExist {

        node = FileTools.normalizeName(node);

        file = new File(directory + "/" + node);

        this.manager = manager;
        this.directory = directory;

        if (!newFile && !file.isFile()) {
            throw new RepositoryException(directory + "/" + node
                    + " is not a file");
        }

        if (!newFile) {
            lastVersion = false;
            refreshProps();
        }

        if (newFile && file.exists()) {
            throw new ResourceAlreadyExists(file.getAbsolutePath()
                    + " already exists");
        } else if (!newFile && !file.exists()) {
            throw new ResourceDoesNotExist(file.getAbsolutePath()
                    + " doesn't exists");
        }

        String suffix;

        if (node.lastIndexOf(".") != -1) {
            suffix = node.substring(node.lastIndexOf("."), node.length());
        } else {
            suffix = ".bin";
        }

        try {
            tmpFile = File.createTempFile("shotoku-file", suffix);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }

        revision = getHistory().getRevisionsCount();
    }

    /*
      * public FileNode(File file) throws RepositoryException { if
      * (file.isFile()) { this.file = file; } else { throw new
      * RepositoryException(file.getAbsolutePath() + " is not a file."); } }
      */

    public String getContent() throws RepositoryException {
        StringBuffer buffer = new StringBuffer();

        try {
            InputStream is = new FileInputStream(
                    (contentChanged || lastVersion) ? tmpFile : file);

            int chr;

            while ((chr = is.read()) != -1) {
                buffer.append((char) chr);
            }

            is.close();
        } catch (FileNotFoundException e) {
            new RepositoryException(e);
        } catch (IOException e) {
            new RepositoryException(e);
        }

        return buffer.toString();
    }

    public void setContent(String content) {
        try {
            OutputStream os = new FileOutputStream(tmpFile);

            for (int i = 0; i < content.length(); i++) {
                os.write(content.charAt(i));
            }

            os.close();
        } catch (FileNotFoundException e) {
            new RepositoryException(e);
        } catch (IOException e) {
            new RepositoryException(e);
        }

        contentChanged = true;
    }

    public void setContent(InputStream is) {
        try {
            OutputStream os = new FileOutputStream(tmpFile);

            int i;

            while ((i = is.read()) != -1) {
                os.write(i);
            }

            os.close();
        } catch (FileNotFoundException e) {
            new RepositoryException(e);
        } catch (IOException e) {
            new RepositoryException(e);
        }

        contentChanged = true;
    }

    public void setContent(byte[] b) {
        try {
            OutputStream os = new FileOutputStream(tmpFile);

            os.write(b);

            os.close();
        } catch (FileNotFoundException e) {
            new RepositoryException(e);
        } catch (IOException e) {
            new RepositoryException(e);
        }

        contentChanged = true;
    }

    public OutputStream getOutputStream() {
        try {
            contentChanged = true;

            return new FileOutputStream(tmpFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public History getHistory() throws RepositoryException {
        return new FileHistory(this, manager);
    }

    public int getRevisionNumber() throws RepositoryException {
        if (lastVersion)
            return getHistory().getRevisionsCount();
        else
            return revision;
    }

    public void setRevision(int revision) {
        lastVersion = true;
        this.revision = revision;
    }

    public void copyToFile(String fileName) throws RepositoryException {
        try {
            OutputStream os = new FileOutputStream(new File(fileName));
            InputStream is = getContentInputStream();

            int b;
            while ((b = is.read()) != -1)
                os.write(b);

            os.close();
            is.close();

        } catch (FileNotFoundException e) {
            new RepositoryException(e);
        } catch (IOException e) {
            new RepositoryException(e);
        }

    }

    public InputStream getContentInputStream() throws RepositoryException {
        try {
            return new FileInputStream(
                    (contentChanged || lastVersion) ? tmpFile : file);
        } catch (FileNotFoundException e) {
            throw new RepositoryException(e);
        }
    }

    public byte[] getContentByteArray() throws RepositoryException {
        InputStream is = getContentInputStream();

        byte[] b = new byte[(int) getLength()];

        try {
            is.read(b);

            is.close();

            return b;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    public long getLength() throws RepositoryException {
        return file.length();
    }

    public long getLastModification() throws RepositoryException {
        return file.lastModified();
    }

    public Date getLastModificationDate() throws RepositoryException {
        return new Date(getLastModification());
    }

    public long getCreated() throws RepositoryException {
        Properties history = FileTools.getHistoryForNode(this, manager);

        if (history.getProperty(CREATE_DATE) != null) {
            return Long.valueOf(history.getProperty(CREATE_DATE));
        }
        else {
            return getLastModification();
        }
    }

    public Date getCreatedDate() throws RepositoryException {
        return new Date(getCreated());
    }

    public String getMimeType() {
        return FileTools.getMimeType(file);
    }

    /**
     * Copies this resource to the given directory. This can't be called if this
     * resource is new and not yet saved.
     *
     * @param dir
     *            Directory to copy this resource to. This can't be a new and
     *            not saved directory.
     * @param newName
     *            New name of this resource (in directory dir after copying).
     * @param logMessage
     *            Log message associated with this resource copy.
     * @throws CopyException
     */
    public void copyTo(Directory dir, String newName, String logMessage)
            throws CopyException {
        try {
            Node newNode = dir.newNode(newName);

            newNode.setContent(getContentByteArray());

            newNode.save(logMessage);
        } catch (RepositoryException e) {
            throw new CopyException(e);
        } catch (ResourceAlreadyExists e) {
            throw new CopyException(e);
        } catch (SaveException e) {
            throw new CopyException(e);
        } catch (NameFormatException e) {
            throw new CopyException(e);
        }
    }

    /**
     * Moves this resource to the given directory. This can't be called if this
     * resource is new and not yet saved. This resource should not be used after
     * performing this operation.
     *
     * @param dir
     *            Directory to move this resource to. This can't be a new and
     *            not saved directory.
     * @param logMessage
     *            Log message associated with this resource move.
     * @throws MoveException
     */
    public void moveTo(Directory dir, String logMessage) throws MoveException {

        File toFile = new File(dir + "/" + file.getName());

        try {
            // get new node
            Node newNode = dir.newNode(file.getName());

            if (toFile.exists()) {
                throw new MoveException("There is already" + file.getName()
                        + " in directory " + dir);
            }

            // get history dirs
            File oldHisDir = FileTools.getHistoryDir(this, manager);
            File newHisDir = FileTools.getHistoryDir((FileNode) newNode,
                    manager);

            if (newHisDir.exists()) {
                throw new MoveException("History for " + newNode.getFullName()
                        + " (" + newHisDir.getAbsolutePath() + ")"
                        + " already exists");
            }

            // move files
            file.renameTo(toFile);
            oldHisDir.renameTo(newHisDir);

            file = toFile;
        } catch (RepositoryException e) {
            new MoveException(e);
        } catch (ResourceAlreadyExists e) {
            new MoveException(e);
        } catch (NameFormatException e) {
            new MoveException(e);
        }

    }

    /**
     * Gets a map of all properties associated with this resource.
     *
     * @return A map of properties associated with this resource.
     * @throws RepositoryException
     */
    public Map<String, String> getProperties() throws RepositoryException {
        refreshProps();
        Map<String, String> map = new HashMap<String, String>();

        for (Object key : props.keySet()) {
            map.put((String) key, props.getProperty((String) key));
        }

        return map;
    }

    private void refreshProps() {
        if (!propsChanged)
            props = FileTools.getPropertiesForNode(this, manager);
    }

    /**
     * Gets the value of the given property.
     *
     * @param propertyName
     *            Name of the property to get.
     * @return Value of the given property.
     * @throws RepositoryException
     */
    public String getProperty(String propertyName) throws RepositoryException {
        refreshProps();
        return props.getProperty(propertyName);
    }

    /**
     * Deletes the the given property.
     *
     * @param propertyName
     *            Name of the property to delete.
     * @throws RepositoryException
     */
    public void deleteProperty(String propertyName) throws RepositoryException {
        refreshProps();
        props.remove(propertyName);
        propsChanged = true;
    }

    /**
     * Sets the value of the given property. Only after saving this change will
     * be persisted.
     *
     * @param propertyName
     *            Name of the property to set. It must begin with a character,
     *            and cannot contain any special characters (so the regexp to
     *            which a property name must match would be [a-z][a-z0-9]*).
     * @param propertyValue
     *            Value of the property to set.
     */
    public void setProperty(String propertyName, String propertyValue) {
        refreshProps();
        props.setProperty(propertyName, propertyValue);
        propsChanged = true;
    }

    /**
     * Gets a directory to which this node/ directory belongs.
     *
     * @return A directory to which this node/ directory belongs. Null if this
     *         directory is already the root directory.
     */
    public Directory getParent() throws RepositoryException {
        return new FileDirectory(file.getParent(), file.getParentFile()
                .getName(), manager);
    }

    /**
     * Saves modified properties and possibly content (in case of nodes).
     *
     * @param logMessage
     *            Log message for saving this node/ directory.
     * @throws SaveException
     * @throws RepositoryException
     */
    public void save(String logMessage) throws SaveException,
            RepositoryException {
        Properties history = FileTools.getHistoryForNode(this, manager);

        // get last props version
        int i = 1;

        while (history.getProperty("ver." + i) != null) {
            i++;
        }

        history.setProperty("ver." + i, logMessage);

        if (i != 1) {
            // get next history file
            File nextVersion = FileTools.getNextHistoryFile(this, manager, i);
            File nextPropFile = FileTools.getNextHistoryPropFile(this, manager,
                    i);

            if (nextVersion.exists()) {
                throw new SaveException(
                        "Conflict in saving: history file exists");
            }

            try {
                nextVersion.createNewFile();
            } catch (IOException e) {
                throw new RepositoryException(e);
            }

            // save props version
            try {
                OutputStream hisProps = new FileOutputStream(nextPropFile);

                // get current props
                FileTools.getPropertiesForNode(this, manager).store(hisProps,
                        FileTools.STORE_TEXT);

                hisProps.close();
            } catch (IOException e) {
                throw new RepositoryException(e);
            }

            // save prevoius version
            try {
                FileInputStream lastContent = new FileInputStream(file);
                FileOutputStream nextVersionOS = new FileOutputStream(
                        nextVersion);

                int chr;

                while ((chr = lastContent.read()) != -1) {
                    nextVersionOS.write(chr);
                }

                lastContent.close();
                nextVersionOS.close();
            } catch (FileNotFoundException e3) {
                throw new SaveException(e3);
            } catch (IOException e3) {
                throw new RepositoryException(e3);
            }
        }

        // new save props and history
        FileOutputStream fos = null;
        FileOutputStream fosPr = null;
        try {
            fos = new FileOutputStream(FileTools.getHistoryFile(this, manager,
                    true));
            fosPr = new FileOutputStream(FileTools.getPropsFile(this, manager));
        } catch (FileNotFoundException e) {
            throw new SaveException(e);
        }

        try {
            if (history.getProperty(CREATE_DATE) == null) {
                // if new file - set current one
                if (!file.exists()) {
                    history.setProperty(CREATE_DATE, String.valueOf((new Date())
                            .getTime()));
                } else {
                    // set create date to last modified (if it was just created
                    // - it will be the right one)
                    history.setProperty(CREATE_DATE, String.valueOf(file
                            .lastModified()));
                }
            }


            history.store(fos, FileTools.STORE_TEXT);
            props.store(fosPr, FileTools.STORE_TEXT);
        } catch (IOException e) {
            try {
                fos.close();
                fosPr.close();
            } catch (IOException e1) {
                throw new RepositoryException(e);
            }
            throw new RepositoryException(e);
        }

        // save new version

        FileOutputStream newFileOS = null;

        try {
            File parent = file.getParentFile();

            if (!parent.exists()) {
                parent.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            newFileOS = new FileOutputStream(file);
        } catch (FileNotFoundException e2) {
            throw new SaveException(e2);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }

        FileInputStream tmpFileIS = null;
        try {
            tmpFileIS = new FileInputStream(tmpFile);
        } catch (FileNotFoundException e2) {
            throw new SaveException(e2);
        }

        int b;

        try {
            while ((b = tmpFileIS.read()) != -1) {
                newFileOS.write(b);
            }
        } catch (IOException e2) {
            throw new SaveException(e2);
        }

        try {
            tmpFileIS.close();
            tmpFileIS.close();
        } catch (IOException e2) {
            throw new SaveException(e2);
        }

        // set bools to false
        lastVersion = contentChanged = propsChanged = false;

        // set current revision
        revision = i;
    }

    /**
     * Gets a log message with which this node/ directory was saved.
     *
     * @return Log message with which this node/ directory was saved. Null, if
     *         the resource is not yet saved or contains modifications.
     * @throws RepositoryException
     */
    public String getLogMessage() throws RepositoryException {

        if (contentChanged)
            return null;

        Properties history = FileTools.getHistoryForNode(this, manager);

        return history.getProperty("ver." + revision);

    }

    /**
     * Deletes this node or directory (immediately, no <code>save()</code> is
     * needed). This resource should not be used after performing this
     * operation.
     *
     * @throws DeleteException
     * @throws RepositoryException
     */
    public void delete() throws DeleteException, RepositoryException {
        File history = FileTools.getHistoryDir(this, manager);

        FileTools
                .deleteDirectoryRecurseivly(history, history.getAbsolutePath());

        if (file.exists()) {
            try {
                if (!file.delete()) {
                    throw new DeleteException("Couldn't delete file "
                            + file.getAbsolutePath());
                }
            } catch (SecurityException e) {
                throw new RepositoryException(e);
            }
        }
    }

    /**
     * Gets the name of this resource (node/ directory).
     *
     * @return Name of this resource (node/ directory). An empty string, if this
     *         is the root directory.
     */
    public String getName() {
        return file.getName();
    }

    /**
     * Gets the full name of this resource, that is, path to this resource
     * relative to the content manager this node was read from.
     *
     * @return Full name of this resource.
     */
    public String getFullName() {
        return file.getAbsolutePath().replace(
                manager.getPathToRepoCurrent() + "/", "");
    }

    public Directory getDirectory() {
        return directory;
    }

    public String getNodeName() {
        return file.getName();
    }

    public void setProperties(Properties props) {
        this.props = props;
        propsChanged = true;
    }

    public void setContentChanged(boolean contentChanged) {
        this.contentChanged = contentChanged;
    }

    public boolean hasModifications() {
        // TODO check
        return contentChanged || propsChanged || lastVersion;
    }
    
    public ContentManager getContentManager() {
        return manager;
    }
}
