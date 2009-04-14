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

import org.jboss.shotoku.*;
import org.jboss.shotoku.exceptions.SaveException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.DeleteException;

import java.util.Map;

/**
 *
 */
public class JcrTest {
    static void printProps(Resource res) {
        System.out.println("-------------------");
        Map<String, String> props = res.getProperties();
        for (String n : props.keySet()) {
            System.out.println(n + " = " + props.get(n));
        }
        System.out.println("-------------------");
    }

    final static String TEST_FILE		= "history-test-1";

    public static void main(String[] args) throws Exception {
        ContentManager.setup();
        ContentManager cm = ContentManager.getContentManager("jcr", "");
        //Directory d = cm.getRootDirectory();

        Directory dir1 = cm.getRootDirectory().newDirectory("a/b");

        Node node1 = dir1.newNode("e/f");

        System.out.println(node1.getFullName());

        /*d.newDirectory("AAA").save("");

        try {
            ContentManager cm2 = ContentManager.getContentManager("AAA");
            System.out.println("CM2: " + cm2);
            System.out.println(cm2.getRootDirectory().getFullName());
        } finally {
            d.getDirectory("AAA").delete();
        } */
        /*d.setProperty("prop3", "val3");
        d.save("");
        d.setProperty("prop3", "val4");
        d.save("");

        System.out.println("d' prop: " + cm.getRootDirectory().getProperty("prop3"));
        System.out.println("d prop: " + d.getProperty("prop3"));
        Map<String, String> props = d.getProperties();
        for (String n : props.keySet()) {
            System.out.println("Name: " + n + "; " + props.get(n));
        }             */

        /*Directory d2 = d.newDirectory("uuu");
        d2.save("x");

        d.getDirectory("uuu").delete();*/

        // Checking if the test directory exists, and if not, creating it.
        /*ContentManager cmTemp = ContentManager.getContentManager("jcr", "");
        try {
            cmTemp.getDirectory("rrrttt");
        } catch (ResourceDoesNotExist e) {
            try {
                cmTemp.getRootDirectory().newDirectory("rrrttt").save("z");
            } catch (Exception e2) {
                throw new RuntimeException("Exception while intializing the test directory", e2);
            }
        }

        System.out.println("--------------");
        for (Directory dd : d.getDirectories()) {
            System.out.println(dd.getFullName());
        }
        System.out.println("--------------");

        d = cmTemp.getRootDirectory();
        System.out.println("--------------");
        for (Directory dd : d.getDirectories()) {
            System.out.println(dd.getFullName());
        }
        System.out.println("--------------");

        System.out.println("--------------");
        for (Directory dd : d.getDirectories()) {
            System.out.println(dd.getFullName());
        }
        System.out.println("--------------");

        cmTemp.getDirectory("rrrttt").delete();   */

        //d.newNode("zzz/node").save("x");

        /*Node n = d.getNode("zzz/node");
        n.setContent("zzz");
        n.save("x");
        System.out.println("--------------");
        System.out.println(d.getDirectory("zzz").getNode("node").getContent());
        System.out.println("--------------");*/
    }
}
