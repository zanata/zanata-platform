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

import org.jboss.shotoku.Node;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.SaveException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class ModifyContentTest extends ShotokuTest {
	private final static String TEST_FILE		= "modify-content-test-1";
	private final static String TEST_CONTENT	= "content 1";
	private final static String TEST_CONTENT_2	= "content 2";
	private final static String TEST_CONTENT_3	= "content 3";
	
	@Override
	protected void setUp() throws Exception {
		Node n = cm.getRootDirectory().newNode(TEST_FILE);
		n.setContent("initial");
		n.save(TEST_FILE);
	}

	public void testOneContentChange() throws ResourceAlreadyExists, ResourceDoesNotExist, RepositoryException, SaveException {
		// Getting the test node.
		Node n = cm.getNode(TEST_FILE);
		
		// Setting content, but not saving.
		n.setContent(TEST_CONTENT);
		
		// Getting the same node again. Its content shouldn't be
		// modified.
		Node n2 = cm.getNode(TEST_FILE);
		assertFalse(TEST_CONTENT.equals(n2.getContent()));
		
		// Saving the first node.
		n.save(TEST_FILE);
		
		// Now both contents should be the same.
		assertTrue(TEST_CONTENT.equals(n2.getContent()));
		
		// Getting a third node, checking its content, it should
		// be modified.
		assertTrue(TEST_CONTENT.equals(cm.getRootDirectory().getNode(
				TEST_FILE).getContent()));
	}
	
	public void testTwoContentChange() throws ResourceAlreadyExists, ResourceDoesNotExist, RepositoryException, SaveException {
		Node n = cm.getNode(TEST_FILE);
		Node n2 = cm.getNode(TEST_FILE);
		
		// Setting and saving content for the first time.
		n.setContent(TEST_CONTENT_2);
		n.save(TEST_FILE);
		
		// Setting and saving content for the second time.
		n2.setContent(TEST_CONTENT_3);
		n2.save(TEST_FILE);
		
		// Checking if the change is there.
		assertTrue(TEST_CONTENT_3.equals(cm.getRootDirectory().getNode(
				TEST_FILE).getContent()));
	}
	
	public void testBackgroundContentChange() throws Exception {
		Node n = cm.getNode(TEST_FILE);
		Node n2 = cm.getNode(TEST_FILE);
		
		assertTrue("initial".equals(n.getContent()));
		
		Node n3 = cm.getNode(TEST_FILE);
		n3.setContent(TEST_CONTENT);
		
		assertTrue("initial".equals(n.getContent()));
		
		n3.save("");
		
		assertTrue(TEST_CONTENT.equals(n.getContent()));
		assertTrue(TEST_CONTENT.equals(n2.getContent()));
		assertTrue(TEST_CONTENT.equals(n3.getContent()));
		
		// Waiting for a wc update.
		Thread.sleep(10*1000);
		
		assertTrue(TEST_CONTENT.equals(n.getContent()));
		assertTrue(TEST_CONTENT.equals(n2.getContent()));
		assertTrue(TEST_CONTENT.equals(n3.getContent()));
	}
	
	@Override
	protected void tearDown() throws Exception {
		try {
			cm.getNode(TEST_FILE).delete();
		} catch (Exception e) {
			
		}
	}
}
