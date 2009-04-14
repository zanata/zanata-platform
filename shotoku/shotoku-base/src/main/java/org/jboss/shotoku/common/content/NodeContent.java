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
package org.jboss.shotoku.common.content;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.tools.Tools;

/**
 * Node content helper class, for converting between various formats.
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class NodeContent {
	private String stringContent;
	private byte[] byteContent;
	
	private File tmpFile;
	private OutputStream tmpFileOs;
    private boolean deleteTmpFile;

    private boolean changed;

	public NodeContent() {
		changed = false;
	}
	
	public NodeContent(byte[] byteContent) {
		this.byteContent = byteContent;
		changed = false;
	}
	
	/* HELPER METHODS */
	
	private void emptyContent() {
		stringContent = null;
		byteContent = null;

        deleteTmpFile();
    }

    private void deleteTmpFile() {
        if ((deleteTmpFile) && (tmpFile != null)) {
            tmpFile.delete();
            tmpFile = null;
        }
    }

    /* CONVERSION METHODS */
	
	public String asString() {
		if (tmpFile != null) {
			try { 
				tmpFileOs.flush();
				tmpFileOs.close();
				stringContent = Tools.getFileString(tmpFile);
			} catch (IOException e) { 
				stringContent = "";
			}

            deleteTmpFile();
        } else if (stringContent == null) {
			if (byteContent == null)
				stringContent = "";
			else
				stringContent = new String(byteContent);
		}
		
		return stringContent;
	}
	
	public byte[] asByteArray() {
		if (tmpFile != null) {
			try { 
				tmpFileOs.flush();
				tmpFileOs.close();
				byteContent = Tools.getFileBytes(tmpFile).array();
			} catch (IOException e) { 
				byteContent = new byte[0];
			}

            deleteTmpFile();
        } else if (byteContent == null) {
			if (stringContent == null)
				byteContent = new byte[0];
			else
				byteContent = stringContent.getBytes();
		}
		
		return byteContent;
	}
	
	public InputStream asInputStream() {
		if (tmpFile != null) {
			try {
				tmpFileOs.flush();
				return new BufferedInputStream(new FileInputStream(tmpFile));
			} catch (FileNotFoundException e) {
				throw new RepositoryException(e);
			} catch (IOException e) {
				throw new RepositoryException(e);
			}
		} else {
			return new ByteArrayInputStream(asByteArray());
		}
	}
	
	/* SETTING METHODS */
	
	public void setContent(InputStream is) throws IOException {
		emptyContent();
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		Tools.transfer(is, boas);
		
		byteContent = boas.toByteArray();
		
		changed = true;
	}
	
	public void setContent(String content) {
		emptyContent();
		stringContent = content;
		
		changed = true;
	}
	
	public void setContent(byte[] bytes) {
		emptyContent();
		byteContent = bytes;
		
		changed = true;
	}
	
	public OutputStream getOutputStream() {
		emptyContent();
		
		tmpFile = Tools.createTemporaryFile();
        deleteTmpFile = true;
        try {
			tmpFileOs = new BufferedOutputStream(new FileOutputStream(tmpFile));
		} catch (FileNotFoundException e) {
			throw new RepositoryException(e);
		}
		changed = true;
		
		return tmpFileOs;
	}
	
	/* VARIOUS METHODS */
	
	public void copyToFile(File file) throws FileNotFoundException, IOException {
		Tools.transfer(asInputStream(), new FileOutputStream(file));
	}
	
	public boolean getChanged() {
		return changed;
	}
	
	/**
	 * Makes the content marked as not-changed. This should be called
	 * after the content has been saved.
	 */
	public void markUnchanged() {
		emptyContent();
		changed = false;
	}
	
	public long getLength() {
		if (stringContent != null) {
			return stringContent.length();
		}
		
		if (tmpFile != null) {
			try {
				tmpFileOs.flush();
			} catch (IOException e) {
			    // Doing nothing.
			}
			return tmpFile.length();
		}
		
		return asByteArray().length;
	}

    /**
     * Copies content to from the given object. The old object shouldn't
     * be used after being copied (in fact, only when it holds a temporary
     * file reference, but you never know).
     * @param content Content to copy.
     */
    public void copyFrom(NodeContent content) {
		if (content != null) {
			if (content.tmpFile != null) {
                this.tmpFile = content.tmpFile;
				this.tmpFileOs = content.tmpFileOs;

                // This NodeContent "takes over" the other's file.
                this.deleteTmpFile = true;
                content.deleteTmpFile = false;
            } else {
				setContent(content.asByteArray());
			}
			
			changed = content.changed;
		}
	}
	
	public boolean isLarge() {
		return tmpFile != null;
	}
	
	@Override
	public void finalize() throws Throwable {
        deleteTmpFile();

        super.finalize();
    }
}
