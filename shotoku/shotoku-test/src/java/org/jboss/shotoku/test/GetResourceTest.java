package org.jboss.shotoku.test;

import org.jboss.shotoku.Node;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.NodeList;
import org.jboss.shotoku.exceptions.*;

import java.util.Arrays;
import java.util.List;
import java.io.IOException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class GetResourceTest extends ShotokuTest {
    @Override
    protected void setUp() throws Exception {
        Directory dir1 = cm.getRootDirectory().newDirectory("a/b");
        Directory dir2 = cm.getRootDirectory().newDirectory("a/c");

        Node node1 = dir1.newNode("e/f");
        Node node2 = dir1.newNode("g");
        Node node3 = dir1.newNode("h");

        cm.save("get-res-test", dir1, dir2, node1, node2, node3);
    }

    public void testGetChildren() throws Exception {
        Directory d;

        // First getting the "a" directory. This should have two child directories.
        d = cm.getRootDirectory().getDirectory("a");
        List<Directory> ld = d.getDirectories();
        assertEquals(2, ld.size());
        assertTrue("b".equals(ld.get(0).getName()));
        assertTrue("c".equals(ld.get(1).getName()));

        // The "a/b" dir should have one child directory and two child nodes.
        ld = cm.getDirectory("a/b").getDirectories();
        assertEquals(1, ld.size());
        assertTrue("e".equals(ld.get(0).getName()));

        NodeList nl = cm.getDirectory("a/b").getNodes();
        assertEquals(2, nl.size());
        assertTrue("g".equals(nl.toList().get(0).getName()));
        assertTrue("h".equals(nl.toList().get(1).getName()));

        // The "a/b/e" dir should have one node child.
        ld = cm.getDirectory("a/b/e").getDirectories();
        assertEquals(0, ld.size());

        nl = cm.getDirectory("a/b/e").getNodes();
        assertEquals(1, nl.size());
        assertTrue("f".equals(nl.toList().get(0).getName()));
    }

    public void testEmptyGet() throws Exception {
        Directory d = cm.getDirectory("a");

        // These should return the same directory.
        assertTrue("a".equals(d.getDirectory("").getName()));
        assertTrue("a".equals(d.getDirectory("   ").getName()));
        assertTrue("a".equals(cm.getDirectory("  a ").getName()));

        // This should return a child directory.
        assertTrue("a/b/e".equals(cm.getDirectory("  /a/b/e ").getFullName()));

        // This should throw an exception.
        try {
            cm.getDirectory("a/b /e");
        } catch (ResourceDoesNotExist e) {
            // This is ok.
            return;
        }

        throw new Exception("Succeeded in getting a non-existent directory!");
    }

    @Override
    protected void tearDown() throws Exception {
        cm.getDirectory("a").delete();
    }
}