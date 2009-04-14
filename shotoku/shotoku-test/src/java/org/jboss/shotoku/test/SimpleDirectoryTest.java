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

import org.jboss.shotoku.Directory;
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
public class SimpleDirectoryTest extends ShotokuTest {
	public void testAddDeleteDirectory() throws Exception {
		// Creating the directory.
		Directory newDir = cm.getRootDirectory().newDirectory("new-dir-test");

		// Saving the dir - it should be created.
		newDir.save("new-dir-test");

		// Creating some nodes and directories in it.
		newDir.newDirectory("subdir1").save("subdir1");
		newDir.newNode("node1").save("node1");
		Node newNode = newDir.newNode("node2");
		newNode.save("node2");
		newNode.setContent("test");
		newNode.save("node2-2");

		assertTrue("test".equals(cm.getNode(
				"new-dir-test/node2").getContent()));
	}

	public void testNames() throws Exception {
		// Checking name of the root directory.
		assertTrue("".equals(cm.getRootDirectory().getName()));

		Directory newDir = cm.getRootDirectory().newDirectory("new-dir-test");
		newDir.save("new-dir-test");
		Node newNode = newDir.newNode("new-node");
		newNode.save("new-node");

		// Checking name of a new directory.
		assertTrue("new-dir-test".equals(newDir.getName()));
		assertTrue("new-dir-test".equals(cm.getRootDirectory().getDirectory(
				"new-dir-test").getName()));

		// Checking name of a new node.
		assertTrue("new-node".equals(newNode.getName()));
		assertTrue("new-node".equals(cm.getRootDirectory().getDirectory(
				"new-dir-test").getNode("new-node").getName()));
	}
	
	@Override
	protected void tearDown() throws ResourceAlreadyExists, DeleteException, ResourceDoesNotExist {
		try {
			Directory newDir = cm.getDirectory("new-dir-test");
			newDir.delete();
		} catch (ResourceDoesNotExist e) {
			// If it does not exist, all the better, no deleting :).
		}
	}
}
