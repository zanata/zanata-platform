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

import org.jboss.shotoku.cache.ShotokuCacheItem;
import org.jboss.shotoku.cache.ShotokuResourceWatcher;
import org.jboss.shotoku.cache.ChangeType;
import org.jboss.shotoku.cache.ShotokuResourceWatcherSplitDescriptor;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.aop.CacheItem;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

import java.util.Map;
import java.util.Set;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class CacheTest extends ShotokuTest {
    private final static String TEST_FILE		= "cache-test-1";
    private final static String TEST_FILE2		= "cache-test-2";
    private final static String TEST_DIR		= "cache-test";

    public static class TestCacheItem extends ShotokuCacheItem<String, String> {
        public void update(String key, String currentObject) {
            put(key, "new");
        }

        public String init(String key) {
            return "init";
        }
    }

    public static class TestCacheItemInterval extends ShotokuCacheItem<String, String> {
        public TestCacheItemInterval() {
            super(1000 * 10 * 6);
        }

        public void update(String key, String currentObject) {
            put(key, "new");
        }

        public String init(String key) {
            return "init";
        }
    }

    public static class TestResWatcherSplit
            extends ShotokuResourceWatcherSplitDescriptor<String, String> {
        protected void update(String key, String currentObject, Set<String> modified,
                              Set<String> added, Set<String> deleted) {
            String val = "";
            for (String path : modified) {
                val += path + "::";
            }

            put(key, val);
        }

        public String init(String key) {
            addWatchedPath(key, TEST_DIR);
            addWatchedPath(key, TEST_DIR + "/" + TEST_FILE);
            addWatchedPath(key, TEST_DIR + "/" + TEST_FILE2);

            return "init";
        }

        protected ContentManager initContentManager(String key) {
            return ContentManager.getContentManager(cm_id, test_dir);
        }
    }

    public static class TestResWatcher extends ShotokuResourceWatcher<String, String> {
        protected void update(String key, String currentObject, Map<String, ChangeType> changes) {
            String val = "";
            for (String path : changes.keySet()) {
                val += path + "::";
            }

            put(key, val);
        }

        public String init(String key) {
            addWatchedPath(key, TEST_DIR);
            addWatchedPath(key, TEST_DIR + "/" + TEST_FILE);
            addWatchedPath(key, TEST_DIR + "/" + TEST_FILE2);

            return "init";
        }

        protected ContentManager initContentManager(String key) {
            return ContentManager.getContentManager(cm_id, test_dir);
        }
    }

    private ShotokuCacheItem<String, String> testCacheItem = new TestCacheItem();
    private ShotokuCacheItem<String, String> testCacheItemInterval = new TestCacheItemInterval();

    private ShotokuResourceWatcher<String, String> testResWatcher = new TestResWatcher();

    private ShotokuResourceWatcher<String, String> testResWatcherSplit = new TestResWatcherSplit();

    @Override
    protected void setUp() throws Exception {
        Directory d = cm.getRootDirectory().newDirectory(TEST_DIR);
        d.save("");
        d.newNode(TEST_FILE).save("");
    }

    private void sleep(long secs) throws Exception{
        try {
            Thread.sleep(1000 * secs);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    private void sleep() throws Exception{
        sleep(30);
    }

    public void testCacheItemInterval() throws Exception {
        assertEquals("init", testCacheItemInterval.get("X"));

        // Waiting for an update for 10 seconds ...
        sleep(10);

        // Should still be the same.
        assertEquals("init", testCacheItemInterval.get("X"));

        sleep(10);
        assertEquals("init", testCacheItemInterval.get("X"));

        sleep(10);
        assertEquals("init", testCacheItemInterval.get("X"));

        sleep(10);
        sleep(10);
        sleep(10);
        sleep(10);
        assertEquals("new", testCacheItemInterval.get("X"));
    }

    public void testCacheItem() throws Exception {
        assertEquals("init", testCacheItem.get("X"));

        // Waiting for an update for 10 seconds ...
        sleep();

        assertEquals("new", testCacheItem.get("X"));
    }

    public void testResWatcher() throws Exception {
        assertEquals("init", testResWatcher.get("Y"));

        // Waiting for an update for 10 seconds ...
        sleep();

        // No update should be executed.
        assertEquals("init", testResWatcher.get("Y"));

        // Updating a node.
        Node n = cm.getNode(TEST_DIR + "/" + TEST_FILE);
        n.setContent("ZZZ");
        n.save("");

        sleep();
        // The cache should get updated.
        assertTrue(testResWatcher.get("Y").contains(TEST_DIR + "/" + TEST_FILE));
        testResWatcher.put("Y", "other content");
        sleep();
        // The cache should not get updated.
        assertEquals("other content", testResWatcher.get("Y"));

        Directory d = cm.getDirectory(TEST_DIR);
        d.setProperty("A", "B");
        d.save("");
        sleep();
        assertTrue(testResWatcher.get("Y").contains(TEST_DIR));

        cm.getDirectory(TEST_DIR).newNode(TEST_FILE2).save("");
        sleep();
        assertTrue(testResWatcher.get("Y").contains(TEST_DIR + "/" + TEST_FILE2));
    }

    public void testResWatcherSplit() throws Exception {
        assertEquals("init", testResWatcherSplit.get("Y"));

        // Updating a node.
        Node n = cm.getNode(TEST_DIR + "/" + TEST_FILE);
        n.setContent("ZZZ");
        n.save("");

        sleep();
        // The cache should get updated.
        assertTrue(testResWatcherSplit.get("Y").contains(TEST_DIR + "/" + TEST_FILE));
        testResWatcherSplit.put("Y", "other content");
        sleep();
        // The cache should not get updated.
        assertEquals("other content", testResWatcherSplit.get("Y"));

        cm.getDirectory(TEST_DIR).newNode(TEST_FILE2).save("");
        sleep();
        assertTrue("".equals(testResWatcherSplit.get("Y")));
    }

    @CacheItem
    private TestCacheItem annCacheItem;

    @CacheItem
    private TestResWatcher annResWatcher;

    /* Annotations version */

    public void testAnnotatedCacheItem() throws Exception {
        if (ContentManager.isEmbedded()) {
            // Embedded doesn't support injects yet.
            return;
        }

        assertEquals("init", annCacheItem.get("Z"));

        // Waiting for an update for 10 seconds ...
		sleep();

        assertEquals("new", annCacheItem.get("Z"));
    }

    public void testAnnotatedResWatcher() throws Exception {
        if (ContentManager.isEmbedded()) {
            // Embedded doesn't support injects yet.
            return;
        }

        assertEquals("init", annResWatcher.get("W"));

        // Updating a node.
        Node n = cm.getNode(TEST_DIR + "/" + TEST_FILE);
        n.setContent("ZZZ");
        n.save("");

        sleep();
        // The cache should get updated.
        assertTrue(annResWatcher.get("W").contains(TEST_DIR + "/" + TEST_FILE));
        annResWatcher.put("W", "other content");
        sleep();
        // The cache should not get updated.
        assertEquals("other content", annResWatcher.get("W"));
    }     

    @Override
    protected void tearDown() throws Exception {
        try {
            cm.getDirectory(TEST_DIR).delete();
        } catch (ResourceDoesNotExist e) {

        }
    }
}
