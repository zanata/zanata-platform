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

import org.jboss.shotoku.History;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.SaveException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class HistoryTest extends ShotokuTest {
	private final static String TEST_FILE		= "history-test-1";
	
	@Override
	protected void setUp() throws Exception {
		Node n = cm.getRootDirectory().newNode(TEST_FILE);
		n.save(TEST_FILE);
	}
	
	public void testHistoryLengthThree() throws ResourceAlreadyExists, ResourceDoesNotExist, RepositoryException, SaveException {
		Node n = cm.getRootDirectory().getNode(TEST_FILE);
		
		// First making a history for the test file.
		n.setContent("c1");
		n.setProperty("p1", "1");
		n.save("log1");
		
		n.setContent("c2");
		n.setProperty("p1", "3");
		n.setProperty("p2", "2");
		n.save("log2");
		
		n.setContent("c3");
		n.setProperty("p2", "y");
		n.save("log3");
		
		// Now getting the history.
		History h = n.getHistory();
		
		// There should be 4 nodes.
		assertEquals(4, h.getRevisionsCount());
		
		// Now checking the content of individual nodes.
		Node n1 = h.getNodeAtRevision(1);
		assertTrue("c1".equals(n1.getContent()));
		assertTrue("1".equals(n1.getProperty("p1")));
		assertNull(n1.getProperty("p2"));
		assertTrue("log1".equals(n1.getLogMessage()));
		
		n1 = h.getNodeAtRevision(2);
		assertTrue("c2".equals(n1.getContent()));
		assertTrue("3".equals(n1.getProperty("p1")));
		assertTrue("2".equals(n1.getProperty("p2")));
		assertTrue("log2".equals(n1.getLogMessage()));
		
		n1 = h.getNodeAtRevision(3);
		assertTrue("c3".equals(n1.getContent()));
		assertTrue("3".equals(n1.getProperty("p1")));
		assertTrue("y".equals(n1.getProperty("p2")));
		assertTrue("log3".equals(n1.getLogMessage()));
	}
	
	@Override
	protected void tearDown() throws Exception {
		cm.getNode(TEST_FILE).delete();
	}
}
