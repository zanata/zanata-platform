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

import org.apache.commons.configuration.Configuration;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Resource;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.exceptions.*;
import org.jboss.shotoku.exceptions.RepositoryException;

import javax.jcr.*;
import javax.jcr.version.VersionException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.lock.LockException;
import java.util.Collection;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 */
public class JcrContentManager extends ContentManager {
    private static JcrConnector connector;

    /*
     * Initialization & node type registration
     */

    public static void setup(String id, Configuration conf)
            throws Exception {
        /*
         * Getting the connector, as defined in the configuration, or the
         * default one.
         */
        String connectorImpl = conf.getString(JcrTools.PROPERTY_CONNECTOR);
        if (connectorImpl != null) {
            connector = (JcrConnector) Thread.currentThread().getContextClassLoader().loadClass(
                    connectorImpl).newInstance();
        } else {
            connector = new JackrabbitJcrConnector();
        }

        connector.init(conf, id);
    }

    /*
    * "Proper" content manager implementation.
    */

    private Repository repository;

    public JcrContentManager(String id, String prefix, Configuration conf) throws Exception {
        super(id, prefix);
        repository = connector.getRepository();
    }

    /**
     * Checks if the given path represents a root directory.
     */
    boolean isRoot(String path) {
        return ("".equals(path) || "/".equals(path));
    }

    /**
     * Gets a JCR-node for the given path. This can be the root node, or a
     * child node. The path should be already prefixed!
     */
    Node getNodeForPath(Session session, String path) throws javax.jcr.RepositoryException {
        if (isRoot(path)) {
            return session.getRootNode();
        } else {
            return session.getRootNode().getNode(path.substring(1));
        }
    }

    /**
     * Checks if a node that can be found under the given path exists and
     * has the given type. If not, a ResourceDoesNotException exception is
     * thrown.
     * @param path
     * @param requiredType
     */
    private void checkResourceExists(String path, String requiredType) throws ResourceDoesNotExist {
        Session session = null;
        Node n;
        try {
            try {
                session = connector.getSession(repository);
                // If this is a root node, then we have to hand-check to type.
                if ((isRoot(path)) && (connector.getDirectoryNodeType().equals(requiredType))) {
                    return;
                }

                if (path.contains(" ")) {
                    throw new ResourceDoesNotExist("Resource name contains a space!");
                }

                n = getNodeForPath(session, path);

                if (!n.getPrimaryNodeType().isNodeType(requiredType)) {
                    throw new ResourceDoesNotExist("Resource exist, but of a different type.");
                }
            } catch (ResourceDoesNotExist e) {
                throw e;  
            } catch (PathNotFoundException e) {
                throw new ResourceDoesNotExist(path);
            } catch (javax.jcr.RepositoryException e) {
                throw new RepositoryException(e);
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }

    /*
     * ContentManager implementation.
     */

    public Directory getRootDirectory() throws RepositoryException {
        try {
            return getDirectory("");
        } catch (ResourceDoesNotExist e) {
            // This shouldn't happen of course (never).
            throw new RepositoryException(e);
        }
    }

    public org.jboss.shotoku.Node getNode(String path)
    throws ResourceDoesNotExist, RepositoryException {
        path = Tools.normalizeSlashes(path, true);
        checkResourceExists(Tools.concatenatePaths(getPrefix(), path),
                connector.getNodeNodeType());
        return new JcrNode(this, repository, connector, path, false);
    }

    public Directory getDirectory(String path)
    throws ResourceDoesNotExist, RepositoryException {
        path = Tools.normalizeSlashes(path, true);
        checkResourceExists(Tools.concatenatePaths(getPrefix(), path),
                connector.getDirectoryNodeType());
        return new JcrDirectory(this, repository, connector, path, false);
    }

    public void save(String logMessage, Collection<Resource> resources)
    throws SaveException, RepositoryException {
        Session session = null;
        try {
            session = connector.getSession(repository);
            for (Resource res : resources) {
                ((JcrResource) res).internalSave(session, logMessage);
            }

            session.save();
        } catch (SaveException e) {
            throw e;
        } catch (LockException e) {
            throw new SaveException(e);
        } catch (AccessDeniedException e) {
            throw new SaveException(e);
        } catch (ConstraintViolationException e) {
            throw new SaveException(e);
        } catch (ItemExistsException e) {
            throw new SaveException(e);
        } catch (NoSuchNodeTypeException e) {
            throw new SaveException(e);
        } catch (InvalidItemStateException e) {
            throw new SaveException(e);
        } catch (ValueFormatException e) {
            throw new SaveException(e);
        } catch (VersionException e) {
            throw new SaveException(e);
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }

    public void delete(Collection<Resource> resources) throws DeleteException, RepositoryException {
        Session session = null;
        try {
            session = connector.getSession(repository);

            for (Resource resource : resources) {
                JcrResource jcrResource = (JcrResource) resource;
                if (!jcrResource.isNew() && !isRoot(jcrResource.getFullPath())) {
                    try {
                        Node toRemove = getNodeForPath(session, jcrResource.getFullPath());
                        // We know that "toRemove" is not a root node.
                        Node parent = toRemove.getParent();

                        /*
                         * To remove a node, we must modify its parent (this
                         * implies a checkout and checkin).
                         */
                        parent.checkout();
                        toRemove.remove();
                        session.save();
                        parent.checkin();
                    } catch (PathNotFoundException e) {
                        // The resource must have been already deleted.
                    }
                }
            }

            session.save();
        } catch (LockException e) {
            throw new DeleteException(e);
        } catch (ConstraintViolationException e) {
            throw new DeleteException(e);
        } catch (VersionException e) {
            throw new DeleteException(e);
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }
}
