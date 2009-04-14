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

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * This interface describes a connection to a JCR implementation and
 * existing repository.
 * @author Adam Warski (adamw@aster.pl)
 */
public interface JcrConnector {
    /**
     * Initializes the jcr implementation. If necessary, should create custom
     * node types as defined in JcrTools (constants ending with _JCR_NAME).
     * Also, should ensure that the root node is versionable.
     * @param conf Content manager
     * @param id
     * @throws Exception
     */
    public void init(Configuration conf, String id) throws Exception;

    /**
     * @return A repository for this JCR implementation.
     * @throws Exception
     */
    public Repository getRepository() throws Exception;

    /**
     * Logs in a user.
     * @param repository
     * @return An open session.
     * @throws Exception
     */
    public Session getSession(Repository repository) throws Exception;

    /**
     * @return JCR node type which should be used for Shotoku nodes.
     */
    public String getNodeNodeType();

    /**
     * @return JCR node type which should be used for Shotoku directories.
     */
    public String getDirectoryNodeType();

    /**
     * @return A property name for a user-set property with the given name.
     * Should be distinct from internal shotoku property names.
     */
    public String createUserPropertyName(String prop);

    /**
     * @return A property name for a shotoku-set property with the given name.
     * Should be distinct from user property names.
     */
    public String createInternalPropertyName(String prop);

    /**
     * @return A property name which should be used for storing node content.
     */
    public String getDataPropertyName();

    /**
     * Modifies the given node so that it is versionable. No save() or
     * versioning operations should be performed on the given node.
     * @param node Node which should be made versionable.
     * @throws RepositoryException
     */
    public void setNodeVersionable(Node node) throws RepositoryException;
}
