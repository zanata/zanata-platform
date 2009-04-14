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

import java.util.HashSet;
import java.util.Set;

import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.Resource;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class AddDeleteTest extends ShotokuTest {
	private final static String TEST_FILE		= "add-delete-test-1";
	private final static String TEST_DIR		= "add-delete-test";
	
	private void checkTestFileNotExists() {
		try {
			cm.getNode(TEST_FILE);
			
			// An exception should be thrown.
			fail("A node which shouldn't exist, does.");
		} catch (ResourceDoesNotExist e) {
			// Getting here means everything is ok.
		}
	}
	
	public void testMultiDelete() throws Exception {
		Directory parent = cm.getRootDirectory().newDirectory(TEST_DIR);
		Directory d1 = parent.newDirectory("dir1");
		Directory d2 = parent.newDirectory("dir2");
		Node n1 = d1.newNode("node1");

        assertTrue(n1.hasModifications());
        assertTrue(d1.hasModifications());

        cm.save("", parent, n1, d2);

        assertFalse(n1.hasModifications());
        assertFalse(d2.hasModifications());
		
        // All directories and nodes should exist.
		parent = cm.getDirectory(TEST_DIR);
		d1 = parent.getDirectory("dir1");
		d2 = parent.getDirectory("dir2");
		n1 = d1.getNode("node1");
		
		// Deleting all and checking that they don't exist.
		Set<Resource> toDelete = new HashSet<Resource>();
		toDelete.add(parent);
		toDelete.add(d1);
		toDelete.add(d2);
		toDelete.add(n1);
		cm.delete(toDelete);
		
		try {
			cm.getNode(TEST_DIR + "/dir1/node1");
			fail();
		} catch (ResourceDoesNotExist e) {
			// This should be thrown.
		}
	}
	
	public void testDeleteWithoutSave() throws Exception {
		Node n = cm.getRootDirectory().newNode(TEST_FILE);
		
		checkTestFileNotExists();
		
		n.delete();
		
		checkTestFileNotExists();
	}
	
	public void testDeleteWithSaveImmediate() throws Exception {
		Node n = cm.getRootDirectory().newNode(TEST_FILE);
		
		checkTestFileNotExists();
		
		n.save(TEST_FILE);
		
		// The file should exist now.
		try {
			cm.getNode(TEST_FILE);
		} catch (ResourceDoesNotExist e) {
			fail(e.getMessage());
		}
		
		n.delete();
		
		checkTestFileNotExists();
	}
	
	public void testDeleteWithSaveDelayed() throws Exception {
		Node n = cm.getRootDirectory().newNode(TEST_FILE);
		
		checkTestFileNotExists();
		
		n.save(TEST_FILE);
		
		// The file should exist now.
		try {
			cm.getNode(TEST_FILE);
		} catch (ResourceDoesNotExist e) {
			fail(e.getMessage());
		}
		
		// Waiting for an update for 10 seconds ...
		try {
			Thread.sleep(1000 * 10);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		
		// The file should still exist.
		try {
			cm.getNode(TEST_FILE);
		} catch (ResourceDoesNotExist e) {
			fail(e.getMessage());
		}
		
		n.delete();
		
		checkTestFileNotExists();
	}
	
	public void testAddAfterDelete() throws Exception {
		// Adding and deleting
		Node n = cm.getRootDirectory().newNode(TEST_FILE);
		n.save(TEST_FILE);
		n.delete();
		
		// Only adding.
		n = cm.getRootDirectory().newNode(TEST_FILE);
		n.save(TEST_FILE);
		
		// Waiting for an update for 10 seconds ...
		try {
			Thread.sleep(1000 * 10);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		
		// The node should exist.
		// The file should still exist.
		try {
			cm.getNode(TEST_FILE);
		} catch (ResourceDoesNotExist e) {
			fail(e.getMessage());
		}
		
		// Finally - deleting the node.
		n.delete();
	}
	
	protected void tearDown() throws Exception {
		try {
			cm.getNode(TEST_FILE).delete();
		} catch (ResourceDoesNotExist e) {
			// If it does not exist - doing nothing.
		}

        try {
			cm.getNode(TEST_DIR).delete();
		} catch (ResourceDoesNotExist e) {
			// If it does not exist - doing nothing.
		}
    }
}
