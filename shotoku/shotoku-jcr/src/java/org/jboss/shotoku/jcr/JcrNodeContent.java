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

import java.io.InputStream;
import java.io.ByteArrayInputStream;

import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.common.content.NodeContent;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.Node;

/**
 * A subclass of NodeContent, which reads content from a given JCR node,
 * if the content is unchanged (so on first content set, this will start
 * behaving like an ordinary NodeContent).
 * @author Adam Warski (adamw@aster.pl)
 */
public class JcrNodeContent extends NodeContent {
    private JcrContentManager jcrCm;
    private String fullPath;
    private JcrConnector connector;
    private Repository repository;

    public JcrNodeContent(JcrContentManager jcrCm, String fullPath,
                          JcrConnector connector, Repository repository) {
        this.jcrCm = jcrCm;
        this.fullPath = fullPath;
        this.connector = connector;
        this.repository = repository;
    }

    Node getNode(Session session) throws javax.jcr.RepositoryException {
         return jcrCm.getNodeForPath(session, fullPath);
    }

    @Override
    public byte[] asByteArray() {
        if (getChanged()) {
            return super.asByteArray();
        } else {
            Session session = null;
            try {
                session = connector.getSession(repository);

                Node node = getNode(session);
                String propertyName = connector.getDataPropertyName();
                if (node.hasProperty(propertyName)) {
                    return node.getProperty(propertyName).getString().getBytes();
                } else {
                    return new byte[0];
                }
            } catch (Exception e) {
                throw new RepositoryException(e);
            } finally {
                JcrTools.safeSessionLogout(session);
            }
        }
    }

    @Override
    public InputStream asInputStream() {
        if (getChanged()) {
            return super.asInputStream();
        } else {
            Session session = null;
            try {
                session = connector.getSession(repository);

                Node node = getNode(session);
                String propertyName = connector.getDataPropertyName();
                if (node.hasProperty(propertyName)) {
                    return node.getProperty(propertyName).getStream();
                } else {
                    return new ByteArrayInputStream(new  byte[0]);
                }
            } catch (Exception e) {
                throw new RepositoryException(e);
            } finally {
                JcrTools.safeSessionLogout(session);
            }
        }
    }

    @Override
    public String asString() {
        if (getChanged()) {
            return super.asString();
        } else {
            Session session = null;
            try {
                session = connector.getSession(repository);

                Node node = getNode(session);
                String propertyName = connector.getDataPropertyName();
                if (node.hasProperty(propertyName)) {
                    return node.getProperty(propertyName).getString();
                } else {
                    return "";
                }
            } catch (Exception e) {
                throw new RepositoryException(e);
            } finally {
                JcrTools.safeSessionLogout(session);
            }
        }
    }

    @Override
    public long getLength() {
        if (getChanged()) {
            return super.getLength();
        } else {
            Session session = null;
            try {
                session = connector.getSession(repository);

                Node node = getNode(session);
                String propertyName = connector.getDataPropertyName();
                if (node.hasProperty(propertyName)) {
                    return node.getProperty(propertyName).getLength();
                } else {
                    return 0;
                }
            } catch (Exception e) {
                throw new RepositoryException(e);
            } finally {
                JcrTools.safeSessionLogout(session);
            }
        }
    }
}
