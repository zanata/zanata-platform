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
package org.jboss.shotoku.test;

import java.io.IOException;
import java.util.Arrays;

import org.jboss.shotoku.Node;
import org.jboss.shotoku.exceptions.DeleteException;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.SaveException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class ContentSettingTest extends ShotokuTest {
	private final static String TEST_FILE		= "content-saving-test-1";
	private final static String TEST_CONTENT	= "content 1\nline 2";
	private final static String TEST_CONTENT_2	= "content 2\nline 2\nline 3";
	private final static String TEST_CONTENT_3	= "content 3\n\nline 3";
	
	@Override
	protected void setUp() throws Exception {
		Node n = cm.getRootDirectory().newNode(TEST_FILE);
		n.save(TEST_FILE);
	}

	private void assertNodeContent(Node n, String content) {
		assertTrue(content.equals(n.getContent()));
		assertTrue(Arrays.equals(content.getBytes(), n.getContentByteArray()));
	}
	
	public void testString() throws ResourceAlreadyExists, ResourceDoesNotExist, RepositoryException, SaveException, DeleteException {
		// Getting the test node.
		Node n = cm.getNode(TEST_FILE);
		
		// Modifying the content.
        assertFalse(n.hasModifications());
        n.setContent(TEST_CONTENT);
        assertTrue(n.hasModifications());
        n.save(TEST_FILE);
        assertFalse(n.hasModifications());
		
        // Checking if changes are visible.
		assertNodeContent(n, TEST_CONTENT);
		assertNodeContent(cm.getRootDirectory().getNode(TEST_FILE), TEST_CONTENT);
	}
	
	public void testByteArray() throws ResourceAlreadyExists, ResourceDoesNotExist, RepositoryException, SaveException{
		// Getting the test node.
		Node n = cm.getNode(TEST_FILE);
		
		// Modifying the content.
		n.setContent(TEST_CONTENT_2.getBytes());
		n.save(TEST_FILE);
		
		// Checking if changes are visible.
		assertNodeContent(n, TEST_CONTENT_2);
		assertNodeContent(cm.getRootDirectory().getNode(TEST_FILE), TEST_CONTENT_2);
	}
	
	public void testOutputStream() throws ResourceAlreadyExists, ResourceDoesNotExist, IOException, RepositoryException, SaveException {
		// Getting the test node.
		Node n = cm.getNode(TEST_FILE);
		
		// Modifying the content.
		n.getOutputStream().write(TEST_CONTENT_3.getBytes());
		n.save(TEST_FILE);
		
		// Checking if changes are visible.
		assertNodeContent(n, TEST_CONTENT_3);
		assertNodeContent(cm.getRootDirectory().getNode(TEST_FILE), TEST_CONTENT_3);
	}
	
	@Override
	protected void tearDown() throws Exception {
		cm.getNode(TEST_FILE).delete();
	}
}
