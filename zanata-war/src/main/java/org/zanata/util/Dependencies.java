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

import java.io.IOException;
import java.util.Properties;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class Dependencies {
    private static final Properties props = new Properties();

    static {
        try {
            props.load(Dependencies.class.getClassLoader()
                    .getResourceAsStream("dependencies.properties"));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Gets the version of a Maven dependency (currently webjars only)
     * @param depName eg org.webjars.bower:commonmark:jar
     * @return
     */
    public static String getVersion(String depName) {
        return props.getProperty(depName);
    }
}
