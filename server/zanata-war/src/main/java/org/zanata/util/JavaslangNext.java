/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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

import javaslang.API;

/**
 * Stand-ins for some of Javaslang 2.1's methods
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @see API
 */
public class JavaslangNext {
    private JavaslangNext() {
    }

    /**
     * A temporary replacement for an implementation used during prototyping.
     * @param <T> The result type of the missing implementation.
     * @return always throws an exception
     * @throws RuntimeException when called
     */
    public static <T> T TODO() {
        throw new RuntimeException("Not Implemented: An implementation is missing.");
    }

    /**
     * A temporary replacement for an implementation used during prototyping.
     * @param <T> The result type of the missing implementation.
     * @param message An error message
     * @return always throws an exception
     * @throws RuntimeException when called
     */
    public static <T> T TODO(String message) {
        throw new RuntimeException("Not Implemented: " + message);
    }

    // TODO bring in Tuple, [Checked]Function helpers?
}
