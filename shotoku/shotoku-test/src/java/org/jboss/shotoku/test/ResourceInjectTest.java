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
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.aop.DirectoryInject;
import org.jboss.shotoku.aop.NodeInject;
import org.jboss.shotoku.exceptions.DeleteException;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.SaveException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class ResourceInjectTest extends ShotokuTest {
	private final static String TEST_NODE	= "inject-test-node";
	private final static String TEST_DIR	= "inject-test-dir";
	private final static String TEST_TEXT	= "text";
	private final static String TEST_TEXT_2	= "text2";
	private final static String TEST_LOG	= "log";
	
	public void setUp() throws Exception {
		Node testNode = cm.getRootDirectory().newNode(TEST_NODE);
		testNode.setContent(TEST_TEXT);
		testNode.save(TEST_LOG);
		
		Directory testDir = cm.getRootDirectory().newDirectory(TEST_DIR);
		testDir.save(TEST_LOG);
	}
	
	@NodeInject(DEFAULT_TEST_DIR + "/" + TEST_NODE)
	private Node injectedNode;
	
	@DirectoryInject(DEFAULT_TEST_DIR + "/" + TEST_DIR)
	private Directory injectedDir;
	
	@NodeInject(DEFAULT_TEST_DIR + "/" + TEST_NODE)
	private String injectedString;
	
	public void testInjects() throws ResourceAlreadyExists, ResourceDoesNotExist {
        if (ContentManager.isEmbedded()) {
            // Embedded doesn't support injects yet.
            return;
        }

        assertTrue(TEST_TEXT.equals(injectedString));
		assertTrue(TEST_TEXT.equals(injectedNode.getContent()));
		assertTrue(TEST_LOG.equals(injectedNode.getLogMessage()));
	}
	
	public void testContentChange() throws ResourceAlreadyExists, ResourceDoesNotExist, RepositoryException, SaveException {
		if (ContentManager.isEmbedded()) {
            // Embedded doesn't support injects yet.
            return;
        }

        injectedNode.setContent(TEST_TEXT_2);
		injectedNode.save(TEST_LOG);
		
		assertTrue(TEST_TEXT_2.equals(injectedString));
		assertTrue(TEST_TEXT_2.equals(injectedNode.getContent()));
		assertTrue(TEST_TEXT_2.equals(cm.getNode(TEST_NODE).getContent()));
	}
	
	public void tearDown() throws ResourceAlreadyExists, DeleteException, ResourceDoesNotExist {
		try {
			cm.getNode(TEST_NODE).delete();
		} catch(ResourceDoesNotExist e) {
			
		}
		
		try {
			cm.getDirectory(TEST_DIR).delete();
		} catch(ResourceDoesNotExist e) {
			
		}
	}
}
