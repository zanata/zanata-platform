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
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.shotoku.Directory;
import org.jboss.shotoku.common.content.NodeContent;
import org.jboss.shotoku.exceptions.NodeReadOnly;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.tmatesoft.svn.core.SVNException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class SvnHistoricNode extends AbstractSvnNode {
	/*
	 * INITIALIZATION AND HELPER METHODS
	 */
	
	private String logMessage;
	private Date commitDate;
	private int revisionRelative;
	private long revisionAbsolute;
    private long currentRevision;
    private Map<String, String> properties;
    private Map<String, String> filteredProperties;
	
    public SvnHistoricNode(String id, String fullPath, SvnContentManager svnCm,
			Date commitDate, String logMessage, long revisionAbsolute, 
			int revisionRelative, long currentRevision) {
		super(id, fullPath, svnCm);
		
		this.logMessage = logMessage;
		this.commitDate = commitDate;
		this.revisionAbsolute = revisionAbsolute;
		this.revisionRelative = revisionRelative;
        this.currentRevision = currentRevision;
    }

	@Override
	protected String getPropertyInternal(String name) {
		loadContentAndProperties();
		return properties.get(name);
	}

	@Override
	protected String getLogMessageInternal() {
		return logMessage;
	}
	
	@Override
	protected Map<String, String> getPropertiesInternal() {
		return filteredProperties;
	}
	
	@Override
	protected long getCopyRevision() throws SVNException {
		return revisionAbsolute;
	}
	
	private void loadContentAndProperties() {
		if (getNodeContent() == null) {
			properties = new HashMap<String, String>();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			SvnTools.getNodeContent(getFullPath(), revisionAbsolute,
					getSvnCm(), baos, properties, true,
                    currentRevision);

            filteredProperties = new HashMap<String, String>(properties);
            SvnTools.filterProperties(filteredProperties);

            setNodeContent(new NodeContent(baos.toByteArray()));
		}
	}
	
	/*
	 * Node IMPLEMENTATION
	 */
	
	@Override
	public String getContent() {
		loadContentAndProperties();
		
		return super.getContent();
	}
	
	@Override
	public int getRevisionNumber() {
		return revisionRelative;
	}
	
	@Override
	public InputStream getContentInputStream() {
		loadContentAndProperties();
		
		return super.getContentInputStream();
	}

	@Override
	public byte[] getContentByteArray() throws RepositoryException {
		loadContentAndProperties();
		
		return super.getContentByteArray();
	}
	
	@Override
	public long getLength() {
		loadContentAndProperties();
		
		return super.getLength();
	}
	
	public long getLastModification() {
		return commitDate.getTime();
	}

    @Override
	public void copyToFile(String path) {
		loadContentAndProperties();
		
		super.copyToFile(path);
	}
	
	@Override
	public void setContent(String content) {
		throw new NodeReadOnly();
	}
	
	@Override
	public void setProperty(String name, String value) {
		throw new NodeReadOnly();
	}
	
	@Override
	public void save(String logMessage) {
		throw new NodeReadOnly();
	}
	
	@Override
	public void delete() {
		throw new NodeReadOnly();
	}

	@Override
	public void setContent(InputStream arg0) {
		throw new NodeReadOnly();
	}

	@Override
	public OutputStream getOutputStream() {
		throw new NodeReadOnly();
	}

	@Override
	public void setContent(byte[] arg0) {
		throw new NodeReadOnly();
	}
	
	@Override
	public void deleteProperty(String name) {
		throw new NodeReadOnly();
	}
	
	@Override
	public void moveTo(Directory d, String logMessage) {
		throw new NodeReadOnly();
	}
}
