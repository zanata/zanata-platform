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
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.SaveException;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class MultiSaveTest extends ShotokuTest {
	private final static String TEST_DIR		= "multi-save-test";
	
	public void testMultiSave() throws Exception {
		Directory newDir1 = cm.getRootDirectory().newDirectory(TEST_DIR);
		
		Directory newDir2 = newDir1.newDirectory("a/b/c");
		Node newNode1 = newDir1.newNode("a/b/d/n1");
		
		Directory newDir3 = newDir2.newDirectory("z");
		Node newNode2 = newDir3.newNode("aaa");
		
		newNode2.setContent("P");
		
		cm.save("", newDir1, newDir2, newNode1, newNode2);
		
		assertTrue("P".equals(cm.getNode(TEST_DIR + "/a/b/c/z/aaa").getContent()));
	}
	
	protected void tearDown() throws Exception {
		try {
			cm.getDirectory(TEST_DIR).delete();
		} catch (ResourceDoesNotExist e) {
			// If it does not exist - doing nothing.
		}
	}
}
