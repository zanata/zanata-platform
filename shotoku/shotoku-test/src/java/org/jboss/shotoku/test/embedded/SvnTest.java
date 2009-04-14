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
package org.jboss.shotoku.test.embedded;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.cache.ShotokuResourceWatcher;
import org.jboss.shotoku.cache.ShotokuCache;
import org.jboss.shotoku.cache.ChangeType;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;

import java.util.Map;
import java.util.Calendar;
import java.io.OutputStream;
import java.io.ObjectOutputStream;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class SvnTest {
    private final static String TEST_FILE		= "cache-test-1";
    private final static String TEST_FILE2		= "cache-test-2";
    private final static String TEST_DIR		= "cache-test";

    private static ShotokuResourceWatcher<String, String> testResWatcher =
            new ShotokuResourceWatcher<String, String>() {
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
                    return ContentManager.getContentManager("shotoku-test");
                }
            };

    private static void sleep() throws Exception{
        try {
            Thread.sleep(1000 * 15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void assertEquals(Object o1, Object o2) {
        if (!o1.toString().equals(o2.toString())) {
            System.out.println("ASSERTION FAILED: " + o1 + " - " + o2);
        }
    }

    public static void main(String[] args) throws Exception {
        ContentManager cm = ContentManager.getContentManager("default", "shotoku-test");

        try {
            Directory root = cm.getRootDirectory();
            Node n = root.newNode("a");

            n.setContent("B\nB\nB\nB\nB");
            n.save("");

            System.out.println("CONTENT: " + root.getNode("a").getContent());

            n = root.getNode("a");

            n.setContent("B\nB\nB\nB\nA");
            n.save("");

            System.out.println("CONTENT: " + root.getNode("a").getContent());
        } finally {
            try {
                cm.getRootDirectory().getNode("a").delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*cm = ContentManager.getContentManager("default", "");
        long t1 = Calendar.getInstance().getTimeInMillis();
        for (Directory d : cm.getRootDirectory().getDirectories()) {
            for (Node n : d.getNodes()) {
                n.getName();
                n.getProperty("description");
            }
        }
        long t2 = Calendar.getInstance().getTimeInMillis();
        System.out.println(t2 - t1);             */

        try {
            /*Directory dir = cm.getRootDirectory().newDirectory("a");
            Node node = cm.getRootDirectory().newNode("z");
            node.setContent("AAA");
            cm.save(dir, node, "");
            node.setContent("BBB");
            node.save("");
                                                               */
            //System.out.println(cm.getNode("z").getHistory().getRevisionsCount());

            //node.copyTo(dir, "y", "");                         
           // System.out.println(cm.getNode("a/y").getContent());
            //System.out.println(cm.getNode("a/y").getHistory().getNodeAtRevision(0).getContent());
        } finally {
            /*try {
                cm.getDirectory("a").delete();
            } catch (ResourceDoesNotExist e) {
            }

            try {
                cm.getNode("z").delete();
            } catch (ResourceDoesNotExist e) {
            } */
        }

        /*try {
            cm.getRootDirectory().newDirectory("b").save("");
            Thread.sleep(10000);
            cm.getDirectory("b").delete();
            cm.getRootDirectory().newDirectory("b").save("");

            Node node = cm.getRootDirectory().newNode("b/a");
            node.save("");

            System.out.println("z");

            OutputStream os = node.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject("ZZZ");
            oos.close();
            node.save("update");
        } finally {
            try {
                cm.getDirectory("b").delete();
            } catch (ResourceDoesNotExist e) {
                e.printStackTrace();
            }
        }     */

            /*try {
            Directory d = cm.getRootDirectory().newDirectory(TEST_DIR);
            d.save("");
            d.newNode(TEST_FILE).save("");

            assertEquals("init", ShotokuCache.get("res-watch-test"));

            // Waiting for an update for 10 seconds ...
            sleep();

            // No update should be executed.
            assertEquals("init", ShotokuCache.get("res-watch-test"));

            // Updating a node.
            Node n = cm.getNode(TEST_DIR + "/" + TEST_FILE);
            n.setContent("ZZZ");
            n.save("");

            sleep();
            // The cache should get updated.
            assertEquals(TEST_DIR + "/" + TEST_FILE, ShotokuCache.get("res-watch-test"));
            ShotokuCache.put("res-watch-test", "other content");
            sleep();
            // The cache should not get updated.
            assertEquals("other content", ShotokuCache.get("res-watch-test"));

            d = cm.getDirectory(TEST_DIR);
            d.setProperty("A", "B");
            d.save("");
            sleep();
            assertEquals(TEST_DIR, ShotokuCache.get("res-watch-test"));

            cm.getDirectory(TEST_DIR).newNode(TEST_FILE2).save("");
            sleep();
            assertEquals(TEST_DIR + "/" + TEST_FILE2, ShotokuCache.get("res-watch-test"));
        } finally {
            try {
                cm.getDirectory(TEST_DIR).delete();
            } catch (ResourceDoesNotExist e) {
                e.printStackTrace();
            }
        }    */
    }
}
