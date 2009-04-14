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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.jboss.shotoku.History;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.common.content.NodeContent;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class SvnNodeProxy extends SvnResourceProxy implements SvnNode {
	private SvnNode currentNode;

	public SvnNodeProxy(String id, String fullPath, SvnContentManager svnCm) {
		super(id, fullPath, svnCm);
	}
	
	public SvnNodeProxy(String id, String fullPath, SvnContentManager svnCm,
			SvnNode initialNode) {
		super(id, fullPath, svnCm, initialNode);
		
		currentNode = initialNode;
	}
	
	protected SvnResource getFileResource() {
		SvnNode oldNode = currentNode;
		currentNode = new SvnFileNode(getId(), getFullPath(), getSvnCm());
		
		if (oldNode != null) {
			currentNode.getNodeContent().copyFrom(oldNode.getNodeContent());
		}
		
		return currentNode;
	}
	
	protected SvnResource getRepoResource() {
		SvnNode oldNode = currentNode;
		currentNode = new SvnRepoNode(getId(), getFullPath(), getSvnCm());
		
		if (oldNode != null) {
			currentNode.getNodeContent().copyFrom(oldNode.getNodeContent());
		}
		
		return currentNode;
	}
	
	/*
	 * SvnNode IMPLEMENTATION
	 */

	public NodeContent getNodeContent() {
		return currentNode.getNodeContent();
	}

	public void setNodeContent(NodeContent content) {
		currentNode.setNodeContent(content);
	}
	
	/*
	 * Node IMPLEMENTATION
	 */

	public void copyToFile(String fileName) throws RepositoryException {
		checkSwitch();
		currentNode.copyToFile(fileName);
	}

	public String getContent() throws RepositoryException {
		checkSwitch();
		return currentNode.getContent();
	}

	public byte[] getContentByteArray() throws RepositoryException {
		checkSwitch();
		return currentNode.getContentByteArray();
	}

	public InputStream getContentInputStream() throws RepositoryException {
		checkSwitch();
		return currentNode.getContentInputStream();
	}

	public History getHistory() throws RepositoryException {
		return currentNode.getHistory();
	}

    public long getCreated() throws RepositoryException {
		checkSwitch();
		return currentNode.getCreated();
	}

    public long getLength() throws RepositoryException {
		checkSwitch();
		return currentNode.getLength();
	}

	public String getMimeType() {
		return currentNode.getMimeType();
	}

	public OutputStream getOutputStream() {
		return currentNode.getOutputStream();
	}

	public int getRevisionNumber() throws RepositoryException {
		return currentNode.getRevisionNumber();
	}

    public String getLogMessage() throws RepositoryException {
		checkSwitch();
		return currentNode.getLogMessage();
	}

    public void setContent(byte[] bytes) {
		currentNode.setContent(bytes);
	}

	public void setContent(InputStream is) {
		currentNode.setContent(is);
	}

	public void setContent(String content) {
		currentNode.setContent(content);
	}

    public Date getCreatedDate() throws RepositoryException {
		// checkSwitch() is called by calling getCreated(), which is
		// called from the implementation of this function in SvnNode.
		return currentNode.getCreatedDate();
	}
}
