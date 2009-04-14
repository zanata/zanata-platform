/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.shotoku.tools;

import javax.management.MalformedObjectNameException;

import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.shotoku.cache.service.RenewableCacheServiceMBean;
import org.jboss.shotoku.cache.service.monitor.RenewableCacheMonitorServiceMBean;

/**
 * Utility, helper functions used internally.
 *
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class CacheTools {
	/**
	 * The first part of automatically generated fqn-s for cache item nodes.
	 */
	public static final String GENERATED_FQN_BASE = "ShotokuRenewableCache";
	
	/**
	 * Name of the default {@link RenewableCacheServiceMBean} mbean.
	 */
	public static final String DEFAULT_RENEWABLE_CACHE_MBEAN = "shotoku:service=RenewableCache";
	
	/**
	 * Name of the default {@link RenewableCacheMonitorServiceMBean} mbean.
	 */
	public static final String DEFAULT_CACHE_MONITOR_MBEAN = "shotoku:service=RenewableCacheMonitor";
	
	/**
	 * Gets a reference to an mbean with the given name.
	 * @param mbeanName Name of the mbean to get.
	 * @param c Class of the mbean to get.
	 * @return A reference to the mbean.
	 * @throws MalformedObjectNameException
	 */
    public static Object getService(String mbeanName,
    		Class<?> c) throws MalformedObjectNameException {
		return MBeanProxyExt.create(c, mbeanName, MBeanServerLocator.locate());
	}

    /**
	 * Checks if the given string is empty (null or "").
	 * 
	 * @param s String to check.
	 * @return True iff the given string is null or equal to "".
	 */
    public static boolean isEmpty(String s) {
        return (s == null) || ("".equals(s));
    }

    /**
     * Converts the given object to a String in a null-safe way.
     * @param o Object to convert.
     * @return Result of o.toString() or null if o is null.
     */
    public static String toString(Object o) {
        if (o == null) {
            return null;
        }

        return o.toString();
    }

    /**
     * Converts the given object to a String in a null-safe way.
     * Never returns a null.
     * @param o Object to convert.
     * @return Result of o.toString() or an emptys tring if o is null.
     */
    public static String toStringNotNull(Object o) {
        if (o == null) {
            return "";
        }

        return o.toString();
    }

    /**
     * Checks if two objects are equal - either both null, or
     * their equals method returns true.
     * @param obj1 First object to compare.
     * @param obj2 Second object to compare.
     * @return True iff both objects are equal to null or if
     * their equals method returns true.
     */
    public static boolean objectsEqual(Object obj1, Object obj2) {
        return (obj1 == null && obj2 == null) ||
                ((obj1 != null) &&  (obj1.equals(obj2)));
    }
    
    /**
     * Next unique id.
     */
    private static int ID_COUNTER = 1;
    
    /**
     * 
     * @return Gets a unique integer id.
     */
    public static synchronized int getNextId() {
    	return ID_COUNTER++;
    }
}
