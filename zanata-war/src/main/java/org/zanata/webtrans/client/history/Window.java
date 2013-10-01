/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.history;

import java.util.List;
import java.util.Map;

/**
 * Wraps calls to the {@link com.google.gwt.user.client.Window} object to allow
 * mocking for testing in a JRE (non-GWT) environment.
 *
 * Does not implement all Window methods.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public interface Window {
    /**
     * @see com.google.gwt.user.client.Window
     */
    public void setTitle(String title);

    /**
     * Wraps calls to the {@link com.google.gwt.user.client.Window.Location}
     * object to allow mocking for testing in a JRE (non-GWT) environment.
     * <p>
     * Does not implement all Window.Location methods.
     * <p>
     * Includes additional helper methods for retrieving query string parameters
     * in a more useful form.
     *
     * @author David Mason, <a
     *         href="mailto:damason@redhat.com">damason@redhat.com</a>
     * @see com.google.gwt.user.client.Window.Location
     */
    public interface Location {
        public static final String PRE_FILTER_QUERY_PARAMETER_KEY = "doc";

        public String getParameter(String name);

        public Map<String, List<String>> getParameterMap();

        public String getHref();

        /**
         *
         * @return list of document paths specified in the URL
         */
        public List<String> getQueryDocuments();
    }
}
