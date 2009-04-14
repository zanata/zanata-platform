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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.jboss.shotoku.History;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.NodeList;
import org.jboss.shotoku.tools.Tools;
import org.jboss.shotoku.tools.Pair;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.common.content.NodeContent;
import org.jboss.shotoku.svn.operations.CopyFileOperation;
import org.jboss.shotoku.svn.operations.ModifyFileOperation;
import org.jboss.shotoku.svn.operations.ResourceOperation;
import org.jboss.shotoku.svn.service.delayed.SetPropertyDelayedOperation;
import org.tmatesoft.svn.core.SVNException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public abstract class AbstractSvnNode extends AbstractSvnResource implements SvnNode {
    /*
     * INITIALIZATION AND HELPER METHODS
     */
    
    private NodeContent content;
    
	public AbstractSvnNode(String id, String fullPath, SvnContentManager svnCm) {
		super(id, fullPath, svnCm);
	}
	
	public void setNodeContent(NodeContent content) {
		this.content = content;
	}
	
	public NodeContent getNodeContent() {
		return content;
	}
	
	@Override
	public boolean hasModifications() {
		return (getNodeContent().getChanged()) || (super.hasModifications());
	}
	
	public void addOperations(Collection<ResourceOperation> ops,
                              SvnContentManager.ContentManagerHolder cmh) {
		if (hasModifications()) {
            ops.add(new ModifyFileOperation(getId(),
                    getFullPath(), content.getChanged() ? getLength() : -1,
					getModifiedProperties(), getDeletedProperties(),
					getNodeContent(), cmh));
		}
	}
	
	protected ResourceOperation getCopyOperation(SvnResource 
			destination, String newName) throws SVNException {
		
		return new CopyFileOperation(getId(), getFullPath(), 
				destination.getFullPath(), newName, 
				getCopyRevision());
	}
	
	@Override
	public void notifySaved() {
		super.notifySaved();
		content.markUnchanged();
	}
	
	@Override
	public void addLargePaths(Set<String> toFill) {
		if (content.isLarge()) {
			toFill.add(getFullPath());
		}
	}
	
	/*
     * Node IMPLEMENTATION
     */
	
	public String getContent() {
		return getNodeContent().asString();
	}
	
	public byte[] getContentByteArray() {
		return getNodeContent().asByteArray();
	}
	
	public void setContent(String stringContent) {
		getNodeContent().setContent(stringContent);
	}
	
	public void setContent(InputStream is) {
		try {
			getNodeContent().setContent(is);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}
	
	public void setContent(byte[] bytes) {
		getNodeContent().setContent(bytes);
	}
	
	public OutputStream getOutputStream() {
		return getNodeContent().getOutputStream();
	}
	
	public InputStream getContentInputStream() {
		return getNodeContent().asInputStream();
	}
	
	public long getLength() {
		return getNodeContent().getLength();
	}
	
	public void copyToFile(String filename) throws RepositoryException {
		try {
			getNodeContent().copyToFile(new File(filename));
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}
	
	public int getRevisionNumber() {
		int historicRevisions = getHistory().getRevisionsCount();
		return historicRevisions + (hasModifications() ? 1 : 0);
	}

    public long getCreated() {
        String createdProp = this.getProperty(SvnTools.INTERNAL_PROP_CREATED);
        if (createdProp != null) {
            // The created date has been cached. No need to compute it again.
            try {
                return Long.parseLong(createdProp);
            } catch (NumberFormatException e) {
                // However, if it is incorrect, we do want to set it again.
            }
        }

        try {
            SvnRevisionInfo firstRevision = SvnTools.getFirstRevisionInformation(
                    getFullPath(), getSvnCm()).get(0);

            long created = firstRevision.getDate().getTime();

            SvnTools.getService().addDelayedOperation(getId(),
                    new SetPropertyDelayedOperation(getFullPath(),
                            SvnTools.INTERNAL_PROP_CREATED,
                            Long.toString(created)));

            return created;
        } catch (IndexOutOfBoundsException e) {
            throw new RepositoryException(e);
        }
    }

    public History getHistory() {
        Pair<List<SvnRevisionInfo>, Long> revisionInfo =
                SvnTools.getAllRevisionsInformation(getFullPath(), getSvnCm());
        List<SvnRevisionInfo> revisions = revisionInfo.getFirst();
		List<Node> nodes = new ArrayList<Node>();
		
		for (int i=0; i<revisions.size(); i++) {
			SvnRevisionInfo revision = revisions.get(i);
			nodes.add(new SvnHistoricNode(getId(), getFullPath(), getSvnCm(),
					revision.getDate(), revision.getMessage(), 
					revision.getRevision(), i, revisionInfo.getSecond()));
		}
		
		return new SvnHistory(new NodeList(nodes));
	}
	
	public String getMimeType() {
		return Tools.getNameBasedMimeType(getName());
	}

    public Date getCreatedDate() {
        return new Date(getCreated());
    }
}
