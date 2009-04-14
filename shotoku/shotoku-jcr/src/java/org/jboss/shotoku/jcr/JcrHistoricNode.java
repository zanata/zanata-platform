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
import org.jboss.shotoku.exceptions.*;
import org.jboss.shotoku.exceptions.RepositoryException;

import javax.jcr.*;
import javax.jcr.version.VersionIterator;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class JcrHistoricNode extends JcrNode {
    private int revisionNumber;

    public JcrHistoricNode(JcrContentManager jcrCm, Repository repository,
                           JcrConnector connector, String fullName, int revisionNumber) {
        super(jcrCm, repository, connector, fullName, false);

        this.revisionNumber = revisionNumber;

        setContent(new JcrHistoricNodeContent(getJcrCm(), getFullPath(), getConnector(),
                getRepository(), this));
    }

    // Helper functions.

    @Override
    String getJcrProperty(String jcrPropertyName) {
        Session session = null;
        try {
            session = getConnector().getSession(getRepository());

            javax.jcr.Node n = getNode(session);
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

    javax.jcr.Node getNode(Session session) throws javax.jcr.RepositoryException {
        javax.jcr.Node base = getJcrCm().getNodeForPath(session, getFullPath());

        VersionIterator vi = base.getVersionHistory().getAllVersions();

        // Skipping the 0-revision (which was created on node create).
        vi.nextVersion();

        // Counting revision-1 revisions/
        for (int i=0; i<revisionNumber; i++) {
            vi.nextVersion();
        }

        /*
         * Now we should be properly positioned; the version should have one
         * node, and that is what we return.
         */
        return vi.nextVersion().getNodes().nextNode();
    }

    // Node implementation.

    @Override
    public boolean hasModifications() {
        return false;
    }

    @Override
    public int getRevisionNumber() throws RepositoryException {
        return revisionNumber;
    }

    @Override
    public void deleteProperty(String propertyName) throws RepositoryException {
        throw new NodeReadOnly();
    }

    @Override
    public void setContent(String content) {
        throw new NodeReadOnly();
    }

    @Override
    public void setContent(InputStream is) {
        throw new NodeReadOnly();
    }

    @Override
    public void setContent(byte[] bytes) {
        throw new NodeReadOnly();
    }

    @Override
    public OutputStream getOutputStream() {
        throw new NodeReadOnly();
    }

    @Override
    public void copyTo(Directory dir, String newName, String logMessage) throws CopyException {
        throw new NodeReadOnly();
    }

    @Override
    public void moveTo(Directory dir, String logMessage) throws MoveException {
        throw new NodeReadOnly();
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {
        throw new NodeReadOnly();
    }

    @Override
    public void save(String logMessage) throws SaveException, RepositoryException {
        throw new NodeReadOnly();
    }

    @Override
    public void delete() throws DeleteException, RepositoryException {
        throw new NodeReadOnly();
    }
}
