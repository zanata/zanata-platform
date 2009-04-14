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

import java.util.Map;

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
public class PropertiesTest extends ShotokuTest {
	private final static String TEST_FILE		= "properties-test-1";
	private final static String PROP_NAME		= "prop1";
	private final static String PROP_VAL_1		= "val1";
	private final static String PROP_VAL_2		= "val2";
	
	@Override
	protected void setUp() throws Exception {
		Node n = cm.getRootDirectory().newNode(TEST_FILE);
		n.save(TEST_FILE);
	}

	public void testOnePropertyChange() throws ResourceAlreadyExists, ResourceDoesNotExist, RepositoryException, SaveException {
		// Getting the test node.
		Node n = cm.getNode(TEST_FILE);
		
		// The file is new, this property shouldn't have a value.
		assertNull(n.getProperty(PROP_NAME));
		
		n.setProperty(PROP_NAME, PROP_VAL_1);

        assertTrue(n.hasModifications());

        // Still, the property shouldn't be set yet on a new node,
		// but should be set on the old one.
		assertNull(cm.getNode(TEST_FILE).getProperty(PROP_NAME));
		assertTrue(PROP_VAL_1.equals(n.getProperty(PROP_NAME)));
		
		// Saving - the change should be persisted.
		n.save(TEST_FILE);
		
		// Now it should be visible everywhere.
		assertTrue(PROP_VAL_1.equals(n.getProperty(PROP_NAME)));
		assertTrue(PROP_VAL_1.equals(cm.getNode(TEST_FILE).getProperty(
				PROP_NAME)));
	}
	
	public void testTwoPropertyChange() throws ResourceAlreadyExists, ResourceDoesNotExist, RepositoryException, SaveException {
		// Getting the test nodes.
		Node n = cm.getNode(TEST_FILE);
		Node n2 = cm.getNode(TEST_FILE);
		
		// The file is new, this property shouldn't have a value.
		assertNull(n.getProperty(PROP_NAME));
		
		// Setting first value.
		n.setProperty(PROP_NAME, PROP_VAL_1);
		n.save(TEST_FILE);
		
		// Setting the second value.
		n2.setProperty(PROP_NAME, PROP_VAL_2);
		n2.save(TEST_FILE);
		
		// Now everywhere the new value should be visible.
		assertTrue(PROP_VAL_2.equals(n.getProperty(PROP_NAME)));
		assertTrue(PROP_VAL_2.equals(n2.getProperty(PROP_NAME)));
		assertTrue(PROP_VAL_2.equals(cm.getNode(TEST_FILE).getProperty(
				PROP_NAME)));
	}
	
	private void checkProperties(Node n) {
		Map<String, String> props = n.getProperties();
		
		assertTrue(3 == props.size());
		assertTrue("v1".equals(props.get("p1")));
		assertTrue("v2".equals(props.get("p2")));
		assertTrue("v3".equals(props.get("p3")));
	}
	
	public void testGetAllProperties() throws Exception {
		Node n = cm.getNode(TEST_FILE);
		
		n.setProperty("p1", "v1");
		n.setProperty("p2", "v2");
		n.setProperty("p3", "v3");
		n.save("a");
		
		checkProperties(n);
		checkProperties(cm.getNode(TEST_FILE));
		
		// Waiting for a WC update.
		Thread.sleep(1000 * 10);
		checkProperties(n);
		checkProperties(cm.getNode(TEST_FILE));
	}
	
	public void testPropertyDelete() throws Exception {
		Node n = cm.getNode(TEST_FILE);
		
		n.setProperty("p1", "v1");
		n.setProperty("p2", "v2");
		
		n.save("");
		
		n.deleteProperty("p2");
		
		assertNull(n.getProperty("p2"));
		assertTrue("v1".equals(n.getProperty("p1")));
		assertTrue(1 == n.getProperties().size());
		
		assertTrue("v1".equals(cm.getNode(TEST_FILE).getProperty("p1")));
		assertTrue("v2".equals(cm.getNode(TEST_FILE).getProperty("p2")));
		assertTrue(2 == cm.getNode(TEST_FILE).getProperties().size());
		
		n.save("");
		
		assertNull(n.getProperty("p2"));
		assertTrue("v1".equals(n.getProperty("p1")));
		assertTrue(1 == n.getProperties().size());
		
		assertNull(cm.getNode(TEST_FILE).getProperty("p2"));
		assertTrue("v1".equals(cm.getNode(TEST_FILE).getProperty("p1")));
		assertTrue(1 == cm.getNode(TEST_FILE).getProperties().size());
		
		// Waiting for a WC update.
		Thread.sleep(1000 * 10);
		
		assertNull(n.getProperty("p2"));
		assertTrue("v1".equals(n.getProperty("p1")));
		assertTrue(1 == n.getProperties().size());
		
		assertNull(cm.getNode(TEST_FILE).getProperty("p2"));
		assertTrue("v1".equals(cm.getNode(TEST_FILE).getProperty("p1")));
		assertTrue(1 == cm.getNode(TEST_FILE).getProperties().size());
	}
	
	@Override
	protected void tearDown() throws DeleteException, ResourceAlreadyExists, ResourceDoesNotExist {
		cm.getNode(TEST_FILE).delete();
	}
}
