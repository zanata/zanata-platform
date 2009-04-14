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

import org.jboss.shotoku.History;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.NodeList;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

import javax.jcr.*;
import javax.jcr.version.VersionIterator;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class JcrHistory implements History {
    private JcrContentManager jcrCm;
    private Repository repository;
    private JcrConnector connector;
    private String fullName;
    private String fullPath;

    private int revisionsCount;

    public JcrHistory(JcrContentManager jcrCm, Repository repository,
                      JcrConnector connector, String fullName, String fullPath) {
        this.jcrCm = jcrCm;
        this.repository = repository;
        this.connector = connector;
        this.fullName = fullName;
        this.fullPath = fullPath;

        revisionsCount = calculateRevisionsCount();
    }

    private int calculateRevisionsCount() {
        Session session = null;
        try {
            session = connector.getSession(repository);

            VersionIterator vi =
                    jcrCm.getNodeForPath(session, fullPath).getVersionHistory().getAllVersions();

            int revCount = 0;
            while (vi.hasNext()) {
                vi.nextVersion();
                revCount++;
            }

            return revCount - 1;
        } catch (Exception e) {
            throw new RepositoryException(e);
        } finally {
            JcrTools.safeSessionLogout(session);
        }
    }

    public int getRevisionsCount() throws RepositoryException {
        return revisionsCount;
    }

    public Node getNodeAtRevision(int revision) throws ResourceDoesNotExist, RepositoryException {
        if (revision > revisionsCount) {
            throw new ResourceDoesNotExist("There is no revision: " + revision +
                    " of " + fullPath);
        }

        return new JcrHistoricNode(jcrCm, repository, connector, fullName, revision);
    }

    public NodeList getAllRevisions() throws RepositoryException {
        List<Node> nodes = new ArrayList<Node>();
        for (int i=0; i<getRevisionsCount(); i++) {
            try {
                nodes.add(getNodeAtRevision(i));
            } catch (ResourceDoesNotExist e) {
                // The node exists ... otherwise we have a strange error :).
                throw new RuntimeException(e);
            }
        }

        return new NodeList(nodes);
    }
}
