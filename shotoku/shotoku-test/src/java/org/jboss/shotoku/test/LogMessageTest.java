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
public class LogMessageTest extends ShotokuTest {
	private final static String TEST_FILE		= "log-message-test-1";
	private final static String TEST_DIR		= "log-message-test-2";

	public void testNodeLogMessage() throws Exception {
		// Creating a new node.
		Node n = cm.getRootDirectory().newNode(TEST_FILE);
		
		// Not yet saved - should be null.
		assertNull(n.getLogMessage());
		
		// Saving and checking if the log message is visible.
		n.save("log1");
		assertTrue("log1".equals(cm.getRootDirectory().getNode(TEST_FILE).getLogMessage()));
		assertTrue("log1".equals(n.getLogMessage()));
		
		// Setting content, but not saving - log message should be null.
		n.setContent("a");
		assertNull(n.getLogMessage());
	}
	
	public void tearDown() throws DeleteException  {
		try {
			cm.getDirectory(TEST_DIR).delete();
		} catch (ResourceDoesNotExist e) {
			
		}
		
		try {
			cm.getNode(TEST_FILE).delete();
		} catch (ResourceDoesNotExist e) {
			
		}
	}
}
