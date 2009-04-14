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
package org.jboss.shotoku.svn;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.common.content.NodeContent;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class SvnRepoNode extends AbstractSvnNode {
    /*
      * INITIALIZATION AND HELPER METHODS
      */

    private Map<String, String> properties;
    private Map<String, String> filteredProperties;
    private SvnRevisionInfo info;

    public SvnRepoNode(String id, String fullPath, SvnContentManager svnCm) {
        super(id, fullPath, svnCm);

        setNodeContent(new NodeContent());
    }

    @Override
    protected String getPropertyInternal(String name) {
        loadContentAndProperties(true);
        return properties.get(name);
    }

    @Override
    protected Map<String, String> getPropertiesInternal() {
        loadContentAndProperties(true);
        return filteredProperties;
    }

    @Override
    protected String getLogMessageInternal() {
        loadInfo();
        return info.getMessage();
    }

    private void loadInfo() {
        info = SvnTools.getCurrentRevisionInformation(getFullPath(),
                getSvnCm()).get(0);
    }

    private void loadContentAndProperties(boolean loadProps) {
        if (loadProps || !getNodeContent().getChanged()) {
            properties = new HashMap<String, String>();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // revNo = -1 because we want the latest content.
            SvnTools.getNodeContent(getFullPath(), -1, getSvnCm(), baos,
                    properties);

            // Filtering out any internal properties.
            filteredProperties = new HashMap<String, String>(properties);
            SvnTools.filterProperties(filteredProperties);

            if (!getNodeContent().getChanged()) {
                setNodeContent(new NodeContent(baos.toByteArray()));
            }
        }
    }

    /*
     * Node IMPLEMENTATION
     */

    public long getLastModification() {
        loadInfo();
        return info.getDate().getTime();
    }

    @Override
    public String getContent() {
        loadContentAndProperties(false);

        return super.getContent();
    }

    @Override
    public InputStream getContentInputStream() {
        loadContentAndProperties(false);

        return super.getContentInputStream();
    }

    @Override
    public byte[] getContentByteArray() throws RepositoryException {
        loadContentAndProperties(false);

        return super.getContentByteArray();
    }

    @Override
    public long getLength() {
        loadContentAndProperties(false);

        return super.getLength();
    }

    @Override
    public String getProperty(String name) {
        loadContentAndProperties(false);

        return super.getProperty(name);
    }

    @Override
    public void copyToFile(String path) {
        loadContentAndProperties(false);

        super.copyToFile(path);
    }
}
