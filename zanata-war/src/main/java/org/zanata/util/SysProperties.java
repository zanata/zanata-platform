/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.zanata.util;

/**
 * This class holds the names of system properties which can control Zanata.
 * NB: most of the system properties here should only be set to override
 * defaults as a temporary workaround.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class SysProperties {
    /**
     * Maximum number of Lucene results to be considered in TM searches.
     */
    public static final String TM_MAX_RESULTS = "zanata.tm.max.results";
    /**
     * Override Lucene boost value for text contents
     */
    public static final String TM_BOOST_CONTENT = "zanata.tm.boost.content";
    /**
     * Override Lucene value for textflowtarget (id)
     */
    public static final String TM_BOOST_TFTID = "zanata.tm.boost.tftid";
    /**
     * Override Lucene value for project (slug)
     */
    public static final String TM_BOOST_PROJECT = "zanata.tm.boost.project";
    /**
     * Override Lucene value for document name (docId)
     */
    public static final String TM_BOOST_DOCID = "zanata.tm.boost.docid";
    /**
     * Override Lucene value for textflow (resId)
     */
    public static final String TM_BOOST_RESID = "zanata.tm.boost.resid";
    /**
     * Override Lucene value for project iteration (slug)
     */
    public static final String TM_BOOST_ITERATION = "zanata.tm.boost.iteration";
    /**
     * Override default lock timeout for @Synchronized beans
     */
    public static final String LOCK_TIMEOUT = "zanata.lock.timeout.millis";

    /**
     * Gets the value of a system property as a float if available,
     * otherwise returning the default value.
     * @param propName name of the system property
     * @param defVal the default to use if the property is missing or can't be converted to float
     * @return the float value
     */
    public static float getFloat(String propName, float defVal) {
        try {
            String prop = System.getProperty(propName);
            if (prop != null) {
                return Float.parseFloat(prop);
            }
            return defVal;
        } catch (IllegalArgumentException | NullPointerException /* | NumberFormatException */ e) {
            return defVal;
        }
    }

    /**
     * Gets the value of a system property as an int if available,
     * otherwise returning the default value.
     * @param propName name of the system property
     * @param defVal the default to use if the property is missing or can't be converted to int
     * @return the int value
     */
    public static int getInt(String propName, int defVal) {
        return Integer.getInteger(propName, defVal);
    }

    /**
     * Gets the value of a system property as a float if available,
     * otherwise returning the default value.
     * @param propName name of the system property
     * @param defVal the default to use if the property is missing or can't be converted to int
     * @return the int value
     */
    public static long getLong(String propName, long defVal) {
        return Long.getLong(propName, defVal);
    }

}
