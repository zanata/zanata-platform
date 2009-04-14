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

/**
 * 
 * @author adamw
 */
public class TimesTest extends ShotokuTest {
	private final static String TEST_DIR =	"last-mod-test";
	@Override
	protected void setUp() throws Exception {
		Directory d = cm.getRootDirectory().newDirectory(TEST_DIR);
		d.save(TEST_DIR);
	}
	
	public void testLastModInNewDir() throws Exception {
		// Creating a new directory.
		Directory newDir = cm.getRootDirectory().getDirectory(TEST_DIR).newDirectory("test");
		newDir.save("");
		
		// Creating a new node.
		Node newNode = cm.getRootDirectory().getDirectory(TEST_DIR).getDirectory("test").newNode("testn");
		newNode.save("");
		
		// Invoking getting of last modification.
		long mod1 = cm.getRootDirectory().getDirectory(TEST_DIR).getDirectory("test").getNode("testn").getLastModification();
        long mod2 = cm.getRootDirectory().getDirectory(TEST_DIR).getDirectory("test").getLastModification();

        // Last modification time of the directory should be the same as
        // last modification of the node.
        assertEquals(mod1, mod2);
    }

    public void testCreated() throws Exception {
		// Creating a new directory.
		Directory newDir = cm.getRootDirectory().getDirectory(TEST_DIR).newDirectory("test");
		newDir.save("");

		// Creating a new node.
		Node newNode = cm.getRootDirectory().getDirectory(TEST_DIR).getDirectory("test").newNode("testn");
		newNode.save("");

        // Waiting a bit ...
        Thread.sleep(1000);

        // Modifying the content.
        newNode.setContent("z");
        newNode.save("");

        // Now created time should be < modified time.
        assertTrue(cm.getRootDirectory().getDirectory(
                TEST_DIR).getDirectory("test").getNode("testn").getCreated() <
                cm.getRootDirectory().getDirectory(TEST_DIR).getDirectory(
                        "test").getNode("testn").getLastModification());
	}

    @Override
	protected void tearDown() throws Exception {
		cm.getDirectory(TEST_DIR).delete();
	}
}
