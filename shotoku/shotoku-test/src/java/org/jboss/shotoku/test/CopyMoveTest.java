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
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.MoveException;

/**
 * 
 * @author Adam Warski (adamw@aster.pl)
 */
public class CopyMoveTest extends ShotokuTest {
    private final static String TEST_DIR = "copy-move-test";
    private final static String TEST_CONTENT = "test content";

    @Override
    protected void setUp() throws Exception {
        Directory parent = cm.getRootDirectory().newDirectory(TEST_DIR);
        Directory dir1 = parent.newDirectory("dir1");
        Directory dir2 = parent.newDirectory("dir2");
        Node n1 = dir1.newNode("node1");
        n1.setContent(TEST_CONTENT);

        cm.save("setup", parent, dir1, dir2, n1);
    }

    public void testHistoryCopy() throws Exception {
        Node original = cm.getNode(TEST_DIR + "/dir1/node1");
        original.setContent("BBB");
        original.save("");
        original.copyTo(cm.getDirectory(TEST_DIR + "/dir2"), "node2", "copy-node");

        Node copy = cm.getNode(TEST_DIR + "/dir2/node2");
        copy.setContent("AAA");
        copy.save("");

        // Checking that we can safely get the root version.
        cm.getNode(TEST_DIR + "/dir2/node2").getHistory().getNodeAtRevision(0).getContent();
    }

    public void testNodeCopy() throws Exception {
        Node original = cm.getNode(TEST_DIR + "/dir1/node1");
        original.copyTo(cm.getDirectory(TEST_DIR + "/dir2"), "node2", "copy-node");

        assertTrue(TEST_CONTENT.equals(original.getContent()));
        assertTrue(TEST_CONTENT.equals(cm.getNode(TEST_DIR + "/dir2/node2").getContent()));

        // Waiting for a WC update.
        Thread.sleep(1000 * 10);

        assertTrue(TEST_CONTENT.equals(cm.getNode(TEST_DIR + "/dir1/node1").getContent()));
        assertTrue(TEST_CONTENT.equals(cm.getNode(TEST_DIR + "/dir2/node2").getContent()));
    }

    public void testNodeMove() throws Exception {
        Node original = cm.getNode(TEST_DIR + "/dir1/node1");
        original.moveTo(cm.getDirectory(TEST_DIR + "/dir2"), "move-node");

        // The old node shouldn't exist.
        try {
            cm.getNode(TEST_DIR + "/dir1/node1");
            fail("A node which shouldn't exist - does");
        } catch (ResourceDoesNotExist e) {

        }

        assertTrue(TEST_CONTENT.equals(cm.getNode(TEST_DIR + "/dir2/node1").getContent()));

        // Waiting for a WC update.
        Thread.sleep(1000 * 10);

        try {
            cm.getNode(TEST_DIR + "/dir1/node1");
            fail("A node which shouldn't exist - does");
        } catch (ResourceDoesNotExist e) {

        }
        assertTrue(TEST_CONTENT.equals(cm.getNode(TEST_DIR + "/dir2/node1").getContent()));
    }

    public void testDirectoryCopy() throws Exception {
        Directory original = cm.getDirectory(TEST_DIR + "/dir1");
        original.copyTo(cm.getDirectory(TEST_DIR + "/dir2"), "dir3", "copy-dir");

        assertTrue(TEST_CONTENT.equals(cm.getNode(TEST_DIR + "/dir2/dir3/node1").getContent()));

        // Waiting for a WC update.
        Thread.sleep(1000 * 10);

        assertTrue(TEST_CONTENT.equals(cm.getNode(TEST_DIR + "/dir2/dir3/node1").getContent()));
    }

    public void testDirectoryMove() throws Exception {
        Directory original = cm.getDirectory(TEST_DIR + "/dir1");
        original.moveTo(cm.getDirectory(TEST_DIR + "/dir2"), "move-dir");

        try {
            cm.getDirectory(TEST_DIR + "/dir1");
            fail("A directory which shouldn't exist - does");
        } catch (ResourceDoesNotExist e) {

        }
        assertTrue(TEST_CONTENT.equals(cm.getNode(TEST_DIR + "/dir2/dir1/node1").getContent()));

        // Waiting for a WC update.
        Thread.sleep(1000 * 10);

        try {
            cm.getDirectory(TEST_DIR + "/dir1");
            fail("A directory which shouldn't exist - does");
        } catch (ResourceDoesNotExist e) {

        }
        assertTrue(TEST_CONTENT.equals(cm.getNode(TEST_DIR + "/dir2/dir1/node1").getContent()));
    }

    public void testMoveToChild() throws Exception {
        Directory original = cm.getDirectory(TEST_DIR + "/dir1");
        original.newDirectory("a/b/c").save("move-to-child1");
        try {
            original.moveTo(cm.getDirectory(TEST_DIR + "/dir1/a/b/c"), "move-to-child2");
        } catch (MoveException e) {
            // This is ok.
            return;
        }

        throw new Exception("Move to a child directory succeeded!");
    }

    @Override
    protected void tearDown() throws DeleteException, ResourceAlreadyExists, ResourceDoesNotExist {
        try {
            cm.getDirectory(TEST_DIR).delete();
        } catch (ResourceDoesNotExist e) {

        }
    }
}
