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
import org.jboss.shotoku.exceptions.RepositoryException;
import org.jboss.shotoku.exceptions.ResourceAlreadyExists;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.exceptions.SaveException;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public class ParentTest extends ShotokuTest {
	private final static String TEST_DIR1		= "parent-test-1";
	private final static String TEST_DIR2		= "parent-test-2";
	private final static String TEST_DIR3		= "parent-test-3";
	
	public void testThreeLevelParents() throws Exception {
		// First creating a simple directory structure.
		Directory new1 = cm.getRootDirectory().newDirectory(TEST_DIR1);
		new1.save(TEST_DIR1);
		
		Directory new2 = new1.newDirectory(TEST_DIR2);
		new2.save(TEST_DIR2);
		
		Directory new3 = new2.newDirectory(TEST_DIR3);
		new3.save(TEST_DIR3);
		
		// Now, having this structure, testing the parents.
				
		// This should be "identical" to new2.
		Directory parentNew3 = new3.getParent();
		
		// This should be "identical" to new1.
		Directory parentNew2 = new2.getParent();
		
		// This should be the root directory.
		Directory parentNew1 = new1.getParent();
		
		// Checking names.
		assertTrue(parentNew3.getName().equals(new2.getName()));
		assertTrue(parentNew2.getName().equals(new1.getName()));
		assertTrue(parentNew1.getName().equals(cm.getRootDirectory().getName()));
		
		// The parents should have appropriate children - trying to
		// get them.
		parentNew1.getDirectory(TEST_DIR1);
		parentNew2.getDirectory(TEST_DIR2);
		parentNew3.getDirectory(TEST_DIR3);
	}
	
	public void tearDown() {
		try {
			cm.getDirectory(TEST_DIR1).delete();
		} catch (Exception e) {
			
		}
	}
}

