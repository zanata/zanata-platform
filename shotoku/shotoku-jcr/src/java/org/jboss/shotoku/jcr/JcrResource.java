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
package org.jboss.shotoku.jcr;

import org.jboss.shotoku.Resource;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.exceptions.*;
import org.jboss.shotoku.exceptions.RepositoryException;

import javax.jcr.*;
import javax.jcr.version.VersionException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.lock.LockException;
import java.util.*;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public abstract class JcrResource implements Resource {
    private String fullPath;
    private String fullName;
    private String name;
    private boolean isNew;
    private Map<String, String> modifiedProperties;
    private Set<String> deletedProperties;
    private Repository repository;
    private JcrConnector connector;
    private JcrContentManager jcrCm;

    public JcrResource(JcrContentManager jcrCm, Repository repository,
                       JcrConnector connector, String fullName, boolean isNew) {
        this.fullName = Tools.normalizeSlashes(fullName, false);
        this.repository = repository;
        this.connector = connector;
        this.jcrCm = jcrCm;
        this.isNew = isNew;

        fullPath = Tools.concatenatePaths(jcrCm.getPrefix(), fullName);

        // Computing the name (last part of fullName).
        int lastSlash = fullName.lastIndexOf('/');
        if (lastSlash == -1) {
            name = fullName;
        } else {
            name = fullName.substring(lastSlash + 1);
        }

        modifiedProperties = new HashMap<String, String>();
        deletedProperties = new HashSet<String>();
    }

    public String getFullPath() {
        return fullPath;
    }

    public Repository getRepository() {
        return repository;
    }

    public JcrConnector getConnector() {
        return connector;
    }

    public JcrContentManager getJcrCm() {
        return jcrCm;
    }

    public boolean isNew() {
        return isNew;
    }

    /**
     * Creates a new JCR node that is described by this object.
     * @param parent Parent of the node to create.
     * @return
     * @throws javax.jcr.RepositoryException
     */
    abstract Node createNode(Node parent) throws javax.jcr.RepositoryException;

    /**
     * Called when this resource is saved, to perform any additional actions.
     * @param thisResource JCR node for this resource.
     * @throws javax.jcr.RepositoryException
     */
    abstract void notifySave(Node thisResource) throws javax.jcr.RepositoryException;

    /**
     * Creates all necessary nodes basing on this resource's path. All
     * non existent nodes from the root directory to the full path of this
     * resource will be created as directory nodes; the last one will be
     * a directory or file node, depending on this resource type.
     * @return Last node created.
     */
    Node createNodes(Session session) throws SaveException, javax.jcr.RepositoryException {
        String[] tokens = getFullPath().split("[/]");

        Node currNode = session.getRootNode();

        for (int i=1; i<tokens.length-1; i++) {
            try {
                currNode = currNode.getNode(tokens[i]);
            } catch (PathNotFoundException pe) {
                // Creating a new directory.
                currNode.checkout();

                Node newNode = currNode.addNode(tokens[i],
                        getConnector().getDirectoryNodeType());
                getConnector().setNodeVersionable(newNode);

                session.save();
                currNode.checkin();

                currNode = newNode;
            }
        }

        // Creating the last node.
        currNode.checkout();

        Node newNode = createNode(currNode);
        getConnector().setNodeVersionable(newNode);

        currNode.save();
        currNode.checkin();

        return newNode;
    }

    void internalSave(Session session, String logMessage)
            throws SaveException, javax.jcr.RepositoryException {
        // Checking if there is anything to save.
        if (!hasModifications()) return;

        Node n;
        long now = Calendar.getInstance().getTimeInMillis();
        if (isNew()) {
            // Creating all the necessary nodes.
            n = createNodes(session);

            // Setting the "created" time.
            n.setProperty(getConnector().createInternalPropertyName(
                    JcrTools.JCR_PROP_CREATED), now);

            isNew = false;
        } else {
            n = jcrCm.getNodeForPath(session, getFullPath());
        }

        // Creating a new version.
        n.checkout();

        // Setting the "modified" time.
        n.setProperty(getConnector().createInternalPropertyName(
                JcrTools.JCR_PROP_LAST_MOD), now);

        // Setting the log property
        n.setProperty(getConnector().createInternalPropertyName(JcrTools.JCR_PROP_LOG),
                logMessage);

        // Saving properties.
        for (String propName : modifiedProperties.keySet()) {
            n.setProperty(getConnector().createUserPropertyName(propName),
                    modifiedProperties.get(propName));
        }

        for (String propName : deletedProperties) {
            n.setProperty(getConnector().createUserPropertyName(propName),
                    (String) null);
        }

        // Clearing any modified properties.
        modifiedProperties.clear();
        deletedProperties.clear();

        // Executing resource-specific actions.
        notifySave(n);

        // Saving the new version.
        n.save();
        n.checkin();
    }

    /**
     * @param jcrPropertyName Name of the property to get (should already have
     * all necessary namespace information).
     * @return Value of the given property as written in a jcr node corresponding
     * to this resource or null if this property is not set.
     */
    String getJcrProperty(String jcrPropertyName) {
        Session session = null;
        try {
            session = getConnector().getSession(getRepository());

            Node n = getJcrCm().getNodeForPath(session, getFullPath());
            if (n.hasProperty(jcrPropertyName)) {
                return n.getProperty(jcrPropertyName).getString();
            } else {
                return null;
            }
        } catch (PathNotFoundException e) {
            return null;
        } catch (ValueFormatException e) {
            return null;
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }

    /*
     * Resource implementation
     */

    public void copyTo(Directory dir, String newName, String logMessage) throws CopyException {
        if (isNew()) {
            throw new CopyException("Resource not yet saved!");
        }

        JcrDirectory jcrDirectory = (JcrDirectory) dir;
        if (jcrDirectory.isNew()) {
            throw new CopyException("Target directory not yet saved!");
        }

        Session session = null;
        try {
            session = getConnector().getSession(getRepository());

            Node originalParent = getJcrCm().getNodeForPath(session, getFullPath()).getParent();
            Node newParent = getJcrCm().getNodeForPath(session, jcrDirectory.getFullPath());
            originalParent.checkout();
            newParent.checkout();

            String newFullPath = Tools.concatenatePaths(jcrDirectory.getFullPath(), newName);
            session.getWorkspace().copy(getFullPath(), newFullPath);

            // Setting the log message.
            Node newNode = getJcrCm().getNodeForPath(session, newFullPath);
            newNode.checkout();
            newNode.setProperty(getConnector().createInternalPropertyName(JcrTools.JCR_PROP_LOG),
                    logMessage);

            session.save();
            newNode.checkin();
            newParent.checkin();
            originalParent.checkin();
        } catch (PathNotFoundException e) {
            throw new CopyException(e);
        } catch (LockException e) {
            throw new CopyException(e);
        } catch (AccessDeniedException e) {
            throw new CopyException(e);
        } catch (ConstraintViolationException e) {
            throw new CopyException(e);
        } catch (ItemExistsException e) {
            throw new CopyException(e);
        } catch (NoSuchNodeTypeException e) {
            throw new CopyException(e);
        } catch (InvalidItemStateException e) {
            throw new CopyException(e);
        } catch (ValueFormatException e) {
            throw new CopyException(e);
        } catch (VersionException e) {
            throw new CopyException(e);
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }

    public void moveTo(Directory dir, String logMessage) throws MoveException {
        if (isNew()) {
            throw new MoveException("Resource not yet saved!");
        }

        JcrDirectory jcrDirectory = (JcrDirectory) dir;
        if (jcrDirectory.isNew()) {
            throw new MoveException("Target directory not yet saved!");
        }

        if ((dir.getFullName() + "/").startsWith(getFullName() + "/")) {
            throw new MoveException("Cannot move a resource to its child!");
        }

        Session session = null;
        try {
            session = getConnector().getSession(getRepository());

            Node originalParent = getJcrCm().getNodeForPath(session, getFullPath()).getParent();
            Node newParent = getJcrCm().getNodeForPath(session, jcrDirectory.getFullPath());
            originalParent.checkout();
            newParent.checkout();

            String newFullPath = Tools.concatenatePaths(jcrDirectory.getFullPath(), getName());
            session.getWorkspace().move(getFullPath(), newFullPath);

            // Setting the log message.
            Node newNode = getJcrCm().getNodeForPath(session, newFullPath);
            newNode.checkout();
            newNode.setProperty(getConnector().createInternalPropertyName(JcrTools.JCR_PROP_LOG),
                    logMessage);

            session.save();
            newNode.checkin();
            newParent.checkin();
            originalParent.checkin();
        } catch (PathNotFoundException e) {
            throw new MoveException(e);
        } catch (LockException e) {
            throw new MoveException(e);
        } catch (AccessDeniedException e) {
            throw new MoveException(e);
        } catch (ConstraintViolationException e) {
            throw new MoveException(e);
        } catch (ItemExistsException e) {
            throw new MoveException(e);
        } catch (NoSuchNodeTypeException e) {
            throw new MoveException(e);
        } catch (InvalidItemStateException e) {
            throw new MoveException(e);
        } catch (ValueFormatException e) {
            throw new MoveException(e);
        } catch (VersionException e) {
            throw new MoveException(e);
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }

    public Map<String, String> getProperties() throws RepositoryException {
        Session session = null;
        try {
            session = getConnector().getSession(getRepository());

            Map<String, String> ret = new HashMap<String, String>();

            if (!isNew()) {
                // Only getting properties from jcr on saved nodes.
                Node n = getJcrCm().getNodeForPath(session, getFullPath());
                PropertyIterator pi = n.getProperties(
                        getConnector().createUserPropertyName("*"));

                while (pi.hasNext()) {
                    Property p = pi.nextProperty();
                    ret.put(JcrTools.removeNamespace(p.getName()), p.getValue().getString());
                }
            }

            ret.putAll(modifiedProperties);
            for (String deletedProp : deletedProperties) {
                ret.remove(deletedProp);
            }

            return ret;
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }

    public String getProperty(String propertyName) throws RepositoryException {
        if (deletedProperties.contains(propertyName)) {
            return null;
        }

        if (modifiedProperties.containsKey(propertyName)) {
            return modifiedProperties.get(propertyName);
        }

        // If this is a new node, no further possibilities.
        if (isNew()) return null;

        return getJcrProperty(getConnector().createUserPropertyName(propertyName));
    }

    public void deleteProperty(String propertyName) throws RepositoryException {
        deletedProperties.add(propertyName);
        modifiedProperties.remove(propertyName);
    }

    public void setProperty(String propertyName, String propertyValue) {
        modifiedProperties.put(propertyName, propertyValue);
        deletedProperties.remove(propertyName);
    }

    public Directory getParent() throws RepositoryException {
        if (getJcrCm().isRoot(getFullName())) {
            // We cannot go higher.
            return null;
        }

        int lastSlash = getFullName().lastIndexOf('/');
        if (lastSlash == -1) {
            return getJcrCm().getRootDirectory();
        } else {
            try {
                return getJcrCm().getDirectory(getFullName().substring(0, lastSlash));
            } catch (ResourceDoesNotExist e) {
                // Parent directory must exist, but who knows ...
                throw new RepositoryException(e);
            }
        }
    }

    public void save(String logMessage) throws SaveException, RepositoryException {
        getJcrCm().save(logMessage, this);
    }

    public void delete() throws DeleteException, RepositoryException {
        getJcrCm().delete(this);
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean hasModifications() {
        return modifiedProperties.size() > 0 || deletedProperties.size() > 0 || isNew();
    }

    public ContentManager getContentManager() {
        return getJcrCm();
    }

    public long getLastModification() throws RepositoryException {
        if (isNew()) {
            return Calendar.getInstance().getTimeInMillis();
        }

        return Long.parseLong(getJcrProperty(
                getConnector().createInternalPropertyName(JcrTools.JCR_PROP_LAST_MOD)));
    }

    public Date getLastModificationDate() throws RepositoryException {
        return new Date(getLastModification());
    }
}
