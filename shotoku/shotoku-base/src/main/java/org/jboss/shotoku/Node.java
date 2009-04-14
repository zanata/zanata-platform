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
package org.jboss.shotoku;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.jboss.shotoku.exceptions.RepositoryException;

/**
 * An interface which should be implemented by classes representing a node, that
 * is a resource, which is versionable and has some text content.
 * 
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public interface Node extends Resource {
	/**
	 * Gets the content of this node, represented as a string.
	 * 
	 * @return Content of this node.
	 * @throws RepositoryException
	 */
	public String getContent() throws RepositoryException;

	/**
	 * Sets the content of this node. Only after saving this change will be
	 * persisted.
	 * 
	 * @param content
	 *            New content of this node.
	 */
	public void setContent(String content);

	/**
	 * Sets this node's content, reading from the given input stream.
	 * 
	 * @param is
	 *            Input stream to read from.
	 */
	public void setContent(InputStream is);
	
	/**
	 * Sets this node's content, given as an array of bytes.
	 * 
	 * @param bytes
	 *            Byte array of new content.
	 */
	public void setContent(byte[] bytes);
	
	/**
	 * Gets an output stream for writing data to the node. The stream will be
	 * automatically closed upon invocation of any of the getContent...() 
	 * methods, getLenght() or the save() method. Using this method for setting
	 * content instead of the other ones, is an indication to the 
	 * implementation that the content will be large, so a temporary file could
	 * be used instead of a in-memory object.
	 * 
	 * @return An output stream for writing data to the node.
	 */
	public OutputStream getOutputStream();

	/**
	 * Gets a history of this node.
	 * 
	 * @return A history of this node.
	 * @throws RepositoryException
	 */
	public History getHistory() throws RepositoryException;

	/**
	 * Gets the revision number of this node.
	 * 
	 * @return Revision number of this node.
	 * @throws RepositoryException
	 */
	public int getRevisionNumber() throws RepositoryException;

    /**
	 * Gets a log message with which this node/ directory was saved.
	 *
	 * @return Log message with which this node/ directory was saved. Null, if
	 *         the resource is not yet saved or contains modifications.
	 * @throws RepositoryException
	 */
	public String getLogMessage() throws RepositoryException;

    /**
	 * Copies this node's content to the given file.
	 * 
	 * @param filename
	 *            Name of the file to which to copy this node's content.
	 * @throws RepositoryException
	 */
	public void copyToFile(String filename) throws RepositoryException;

	/**
	 * Gets the content of this node as an input stream.
	 * 
	 * @return Content of this node as an input stream.
	 * @throws RepositoryException
	 */
	public InputStream getContentInputStream() throws RepositoryException;
	
	/**
	 * Gets the content of this node as a byte array.
	 * 
	 * @return Content of this node as a byte array.
	 * @throws RepositoryException
	 */
	public byte[] getContentByteArray() throws RepositoryException;

	/**
	 * Gets the length of this node's content.
	 * 
	 * @return Length of this node's content.
	 * @throws RepositoryException
	 */
	public long getLength() throws RepositoryException;

    /**
	 * Gets the mime type of this node.
	 * 
	 * @return Mime type of this node.
	 */
	public String getMimeType();

    /**
     * Gets the time of creation of this node.
     *
     * @return Time of creation of this node.
     * @throws RepositoryException
     */
    public long getCreated() throws RepositoryException;

    /**
     * Gets the time of creation of this node, represented as a date
     * object.
     *
     * @return Time of creation of this node, represented as a date.
     * @throws RepositoryException
     */
    public Date getCreatedDate() throws RepositoryException;
}
