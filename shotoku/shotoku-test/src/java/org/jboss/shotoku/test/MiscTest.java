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

import org.jboss.shotoku.Node;
import org.jboss.shotoku.exceptions.*;

import java.util.Arrays;
import java.io.IOException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class MiscTest extends ShotokuTest {
    private final static String TEST_FILE		= "content-saving-test-1.html";
    private final static String TEST_CONTENT	= "content 1\nline 2";

    @Override
    protected void setUp() throws Exception {
        Node n = cm.getRootDirectory().newNode(TEST_FILE);
        n.setContent(TEST_CONTENT);
        n.save(TEST_FILE);
    }

    public void testHtmlMimeType() throws Exception {
        Node n = cm.getNode(TEST_FILE);

        assertTrue("text/html".equals(n.getMimeType()));
    }

    public void testLength() throws Exception {
        Node n = cm.getNode(TEST_FILE);

        assertEquals(TEST_CONTENT.length(), n.getLength());
    }

    @Override
    protected void tearDown() throws Exception {
        cm.getNode(TEST_FILE).delete();
    }
}
