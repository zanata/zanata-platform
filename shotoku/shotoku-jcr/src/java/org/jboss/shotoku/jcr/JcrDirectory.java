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

import org.jboss.shotoku.Directory;
import org.jboss.shotoku.NodeList;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.NameFormatException;

import javax.jcr.*;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class JcrDirectory extends JcrResource implements Directory {
    public JcrDirectory(JcrContentManager jcrCm, Repository repository,
                        JcrConnector connector, String fullName, boolean isNew) {
        super(jcrCm, repository, connector, fullName, isNew);
    }

    /**
     * Checks if a node with the given path doesn't exist.
     * @param path Path to check.
     */
    private void checkResourceDoesntExists(String path)
    throws ResourceAlreadyExists {
        Session session = null;
        try {
            session = getConnector().getSession(getRepository());

            /*
             * Trying to get the node; this should thrown an exception if the
             * node doesn't exist.
             */
            getJcrCm().getNodeForPath(session, path);
        } catch (PathNotFoundException e) {
            return;
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            JcrTools.safeSessionLogout(session);
        }

        throw new ResourceAlreadyExists(path);
    }

    javax.jcr.Node createNode(javax.jcr.Node parent)
    throws javax.jcr.RepositoryException {
        return parent.addNode(getName(), getConnector().getDirectoryNodeType());
    }

    void notifySave(javax.jcr.Node thisResource) throws javax.jcr.RepositoryException {
        // No actions necessary.
    }

    /**
     * Checks that a resource with the given name can be created in this
     * directory (if not, an appropriate exception is thrown).
     * @param name Name of the new resource to check.
     * @return A full name of the new resource.
     * @throws ResourceAlreadyExists
     * @throws NameFormatException
     * @throws RepositoryException
     */
    private String checkNewResource(String name) throws ResourceAlreadyExists,
            NameFormatException, RepositoryException {
        String newFullName = Tools.concatenatePaths(getFullName(),
                Tools.normalizeSlashes(name, true));
        Tools.checkName(newFullName);
        checkResourceDoesntExists(
                Tools.concatenatePaths(getJcrCm().getPrefix(), newFullName));

        return newFullName;
    }

    /*
     * Directory implementation
     */

    public NodeList getNodes() throws RepositoryException {
        Session session = null;
        try {
            session = getConnector().getSession(getRepository());

            javax.jcr.Node n = getJcrCm().getNodeForPath(session, getFullPath());
            NodeIterator ni = n.getNodes();

            List<Node> ret = new ArrayList<Node>();
            while (ni.hasNext()) {
                javax.jcr.Node nextNode = ni.nextNode();

                if (nextNode.getPrimaryNodeType().isNodeType(
                        getConnector().getNodeNodeType())) {
                    ret.add(getJcrCm().getNode(
                        Tools.concatenatePaths(getFullName(), nextNode.getName())));
                }
            }

            return new NodeList(ret);
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }

    public List<Directory> getDirectories() throws RepositoryException {
        Session session = null;
        try {
            session = getConnector().getSession(getRepository());

            javax.jcr.Node n = getJcrCm().getNodeForPath(session, getFullPath());
            NodeIterator ni = n.getNodes();

            List<Directory> ret = new ArrayList<Directory>();
            while (ni.hasNext()) {
                javax.jcr.Node nextNode = ni.nextNode();

                if (nextNode.getPrimaryNodeType().isNodeType(
                        getConnector().getDirectoryNodeType())) {
                    ret.add(getJcrCm().getDirectory(
                        Tools.concatenatePaths(getFullName(), nextNode.getName())));
                }
            }

            return ret;
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }

    public Node getNode(String name) throws RepositoryException, ResourceDoesNotExist {
        return getJcrCm().getNode(Tools.concatenatePaths(getFullName(), name));
    }

    public Directory getDirectory(String name) throws RepositoryException, ResourceDoesNotExist {
        return getJcrCm().getDirectory(Tools.concatenatePaths(getFullName(), name));
    }

    public Node newNode(String name) throws ResourceAlreadyExists,
            NameFormatException, RepositoryException {
        return new JcrNode(getJcrCm(), getRepository(),
                getConnector(), checkNewResource(name), true);
    }

    public Directory newDirectory(String name) throws ResourceAlreadyExists,
            NameFormatException, RepositoryException {
        return new JcrDirectory(getJcrCm(), getRepository(),
                getConnector(), checkNewResource(name), true);
    }

    public boolean hasIndex(String propertyName) throws RepositoryException {
        throw new RuntimeException("Operation not supported");
    }

    public void setIndex(String propertyName, boolean index) throws RepositoryException {
        throw new RuntimeException("Operation not supported");
    }
}
