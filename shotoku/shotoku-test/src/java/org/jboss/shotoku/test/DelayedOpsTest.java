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

import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.Resource;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class DelayedOpsTest extends ShotokuTest {
    private final static String TEST_FILE		= "delayed-op-test";

    protected void setUp() throws Exception {
        cm.getRootDirectory().newNode(TEST_FILE).save("");
    }

    public void testCreated() throws Exception {
        long created = cm.getRootDirectory().getNode(TEST_FILE).getCreated();

        // Waiting for the delayed op to commit.
        Thread.sleep(25000);

        long created2 = cm.getRootDirectory().getNode(TEST_FILE).getCreated();
        long created3 = Long.parseLong(cm.getRootDirectory().getNode(TEST_FILE).
                getProperty("shotoku:created"));

        assertEquals(created, created2);
        assertEquals(created, created3);
    }

    protected void tearDown() throws Exception {
        try {
            cm.getNode(TEST_FILE).delete();
        } catch (ResourceDoesNotExist e) {
            // If it does not exist - doing nothing.
        }
    }
}
