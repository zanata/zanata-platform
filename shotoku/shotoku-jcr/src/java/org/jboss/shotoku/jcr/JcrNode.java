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

import org.jboss.shotoku.Node;
import org.jboss.shotoku.History;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.common.content.NodeContent;
import org.jboss.shotoku.exceptions.RepositoryException;

import javax.jcr.Repository;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.util.Calendar;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class JcrNode extends JcrResource implements Node {
    private NodeContent content;

    private NodeContent getJcrNodeContent() {
        return new JcrNodeContent(getJcrCm(), getFullPath(),
                    getConnector(), getRepository());
    }

    public JcrNode(JcrContentManager jcrCm, Repository repository,
                   JcrConnector connector, String fullName, boolean isNew) {
        super(jcrCm, repository, connector, fullName, isNew);

        if (isNew) {
            setContent(new NodeContent());
        } else {
            setContent(getJcrNodeContent());
        }
    }

    javax.jcr.Node createNode(javax.jcr.Node parent)
    throws javax.jcr.RepositoryException {
        return parent.addNode(getName(), getConnector().getNodeNodeType());
    }

    void notifySave(javax.jcr.Node thisResource) throws javax.jcr.RepositoryException {
        if (isNew() || content.getChanged()) {
            thisResource.setProperty(getConnector().getDataPropertyName(),
                    content.asInputStream());
        }

        // Clearing content.
        content = getJcrNodeContent();
    }

    void setContent(NodeContent content) {
        this.content = content;
    }

    /*
     * Node implementation
     */

    public boolean hasModifications() {
        return content.getChanged() || super.hasModifications();
    }

    public String getContent() throws RepositoryException {
        return content.asString();
    }

    public void setContent(String content) {
        this.content.setContent(content);
    }

    public void setContent(InputStream is) {
        try {
            content.setContent(is);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    public void setContent(byte[] bytes) {
        content.setContent(bytes);
    }

    public OutputStream getOutputStream() {
        return content.getOutputStream();
    }

    public History getHistory() throws RepositoryException {
        return new JcrHistory(getJcrCm(), getRepository(), getConnector(), getFullName(),
                getFullPath());
    }

    public int getRevisionNumber() throws RepositoryException {
        int allRevisions = getHistory().getRevisionsCount();
        return allRevisions + (hasModifications() ? 1 : 0);
    }

    public String getLogMessage() throws RepositoryException {
        if (hasModifications()) {
            return null;
        }

        return getJcrProperty(getConnector().createInternalPropertyName(JcrTools.JCR_PROP_LOG));
    }

    public void copyToFile(String filename) throws RepositoryException {
        try {
            content.copyToFile(new File(filename));
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    public InputStream getContentInputStream() throws RepositoryException {
        return content.asInputStream();
    }

    public byte[] getContentByteArray() throws RepositoryException {
        return content.asByteArray();
    }

    public long getLength() throws RepositoryException {
        return content.getLength();
    }

    public long getCreated() throws RepositoryException {
        if (isNew()) {
            return Calendar.getInstance().getTimeInMillis();
        }

        return Long.parseLong(getJcrProperty(
                getConnector().createInternalPropertyName(JcrTools.JCR_PROP_CREATED)));
    }

    public Date getCreatedDate() throws RepositoryException {
        return new Date(getCreated());
    }

    public String getMimeType() {
        return Tools.getNameBasedMimeType(getName());
    }
}
