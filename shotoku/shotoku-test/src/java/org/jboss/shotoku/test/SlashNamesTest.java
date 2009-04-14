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

import org.jboss.shotoku.ContentManager;
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
public class SlashNamesTest extends ShotokuTest {
	private final static String TEST_DIR	= "slash-test-dir";
	private final static String TEST_NESTED_DIR		= "a/b/c";
	private final static String TEST_NESTED_NODE	= "h/j/k";
	private final static String TEST_CONTENT	= "T";
	
	public void setUp() throws Exception {
		cm.getRootDirectory().newDirectory(TEST_DIR).save(TEST_DIR);
	}
	
	public void testNested() throws Exception {
		Directory base = cm.getDirectory(TEST_DIR);
		Directory nested1 = base.newDirectory(TEST_NESTED_DIR);
		Node nested2 = base.newNode(TEST_NESTED_NODE);
		
		assertTrue((TEST_DIR + "/" + TEST_NESTED_DIR).equals(nested1.getFullName()));
		assertTrue((TEST_DIR + "/" + TEST_NESTED_NODE).equals(nested2.getFullName()));
		
		nested1.save("");
		
		nested2.setContent(TEST_CONTENT);
		nested2.save("");
		
		// We should be able to get the directories and node.
		base.getDirectory("a").getDirectory("b").getDirectory("c");
		base.getDirectory(TEST_NESTED_DIR);
		
		assertTrue(TEST_CONTENT.equals(base.getDirectory("h").
				getDirectory("j").getNode("k").getContent()));
		assertTrue(TEST_CONTENT.equals(base.getNode(TEST_NESTED_NODE).getContent()));
	}
	
	public void testNestedExisting() throws Exception {
		Directory base = cm.getDirectory(TEST_DIR);
		base.newDirectory("h").save("h");
		
		Node nested = base.newNode(TEST_NESTED_NODE);
		assertTrue((TEST_DIR + "/" + TEST_NESTED_NODE).equals(nested.getFullName()));
		
		nested.setContent(TEST_CONTENT);
		nested.save(TEST_NESTED_NODE);
		
		assertTrue(TEST_CONTENT.equals(base.getDirectory("h").
				getDirectory("j").getNode("k").getContent()));
		assertTrue(TEST_CONTENT.equals(base.getNode(TEST_NESTED_NODE).getContent()));
	}
	
	/**
	 * Checks if content managers work with prefixes that contain unnecessary /.
	 */
	public void testSlashesInCmPrefix() throws ResourceAlreadyExists, ResourceDoesNotExist {
		ContentManager cm1 = ContentManager.getContentManager(cm_id, "/shotoku-test/");
		cm1.getRootDirectory().getDirectory(TEST_DIR);
		
		ContentManager cm2 = ContentManager.getContentManager(cm_id, 
				"//shotoku-test///" + TEST_DIR + "///");
		cm2.getRootDirectory();
	}

	public void tearDown() throws ResourceAlreadyExists, DeleteException, ResourceDoesNotExist {
		try {
			cm.getDirectory(TEST_DIR).delete();
		} catch (ResourceDoesNotExist e) {
			
		}
	}
}
