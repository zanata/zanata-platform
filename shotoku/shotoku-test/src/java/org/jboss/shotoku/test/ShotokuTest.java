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

import org.apache.log4j.Logger;
import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.aop.Inject;
import org.jboss.shotoku.exceptions.ResourceDoesNotExist;
import org.jboss.shotoku.tools.Constants;

import junit.framework.TestCase;

/**
 * @author Adam Warski (adamw@aster.pl)
 * @author Damon Sicore (damon@sicore.com)
 */
public abstract class ShotokuTest extends TestCase {
	protected final static String PROPERTIES_CM_ID = Constants.PROPERTIES_INTERNAL + ".tests.id";
	protected final static String PROPERTIES_TEST_DIR = Constants.PROPERTIES_INTERNAL + ".tests.dir";
	
	protected final static String DEFAULT_CM_ID = "default";
	protected final static String DEFAULT_TEST_DIR = "shotoku-test";
	
	protected static String cm_id = DEFAULT_CM_ID;
	protected static String test_dir = DEFAULT_TEST_DIR; 
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	@Inject(prefix=DEFAULT_TEST_DIR)
	protected ContentManager cm;
	
	public ShotokuTest() {
		// Annotations don't work in embedded mode.
		if (ContentManager.isEmbedded()) {
			cm = ContentManager.getContentManager(cm_id, test_dir);
		}
	}
	
	static {
		// In embedded mode, setup must be called manually.
		if (ContentManager.isEmbedded()) {
			ContentManager.setup();
			cm_id = ContentManager.getProperty(PROPERTIES_CM_ID, DEFAULT_CM_ID);
			test_dir = ContentManager.getProperty(PROPERTIES_TEST_DIR, DEFAULT_TEST_DIR);
		}
		
		// Checking if the test directory exists, and if not, creating it.
		ContentManager cmTemp = ContentManager.getContentManager(cm_id, "");
		try {
			cmTemp.getDirectory(test_dir);
		} catch (ResourceDoesNotExist e) {
			try {
				cmTemp.getRootDirectory().newDirectory(test_dir).save("");
			} catch (Exception e2) {
				throw new RuntimeException("Exception while intializing the test directory", e2);
			} 
		}
	}
}
