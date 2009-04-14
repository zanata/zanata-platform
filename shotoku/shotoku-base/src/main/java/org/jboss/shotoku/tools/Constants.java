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
package org.jboss.shotoku.tools;

/**
 * Constatns used in Shotoku classes.
 * @author Adam Warski (adamw@aster.pl)
 */
public class Constants {
	public static final String PROPERTIES_FILE = "/shotoku.properties";
	public static final String PROPERTIES_ID_DEFAULT = "shotoku.id.default";
	public static final String PROPERTIES_IDS = "shotoku.ids";
	public static final String PROPERTIES_PREFIX = "shotoku";
	public static final String PROPERTIES_INTERNAL = PROPERTIES_PREFIX + ".internal";
	public static final String PROPERTIES_IMPL_SUFFIX = "implementation";
	
	public static final String PROPERTIES_EMBEDDED = PROPERTIES_PREFIX + ".embedded";
	public static final String PROPERTIES_TRANSFER_BUF_SIZE = PROPERTIES_PREFIX + ".transfer.buffer.size";

    private static final String VELOCITY_RL = "shotoku.resource.loader";
	public static final String VELOCITY_RL_PREFIX = VELOCITY_RL + ".prefix";
	public static final String VELOCITY_RL_ID = VELOCITY_RL + ".id";
	public static final String VELOCITY_PROPERTIES_FILE = "/velocity.properties";
	
	public static final String SETUP_FUNCTION = "setup";

    /*
	 * Default values
	 */
	public static final String DEFAULT_ID 				= "default";
	public static final int DEFAULT_TRANSFER_BUF_SIZE	= 1024;

    /**
	 * <code>SHOTOKU_SERVICE_NAME</code> - name under which the shotoku service is
	 * registered.
	 */
	public final static String SHOTOKU_SERVICE_NAME = "shotoku:service=shotoku";

    /**
     * Default key base prefix.
     */
    public final static String SHOTOKU_CACHE_KEY_BASE = "shotoku:cacheitem:keybase:";

    /**
     * A string denoting "true" value.
     */
    public static final String TRUE     = "true";

    /**
     * A string denoting "false" value.
     */
    public static final String FALSE    = "false";
}
