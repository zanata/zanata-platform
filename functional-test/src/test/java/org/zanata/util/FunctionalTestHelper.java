/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.util;

/**
 * The assume functions of junit skip tests if the preconditions are not met.
 * This is not favourable. If the test system cannot properly prepare the
 * environment then the test execution is not valid.
 *
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class FunctionalTestHelper {

    /**
     * Assume a condition is true, and throw an exception if the assumption
     * fails
     *
     * @param message
     *            The assumption made
     * @param condition
     *            the result of the assumption challenge
     * @throws RuntimeException
     *             if the challenge result is false
     */
    public static void assumeTrue(String message, boolean condition) {
        if (!condition)
            throw new RuntimeException(message);
    }

    /**
     * Assume a condition is false, and throw an exception if the assumption
     * fails
     *
     * @param message
     *            The assumption made
     * @param condition
     *            the result of the assumption challenge
     * @throws RuntimeException
     *             if the challenge result is true
     */
    public static void assumeFalse(String message, boolean condition) {
        if (condition)
            throw new RuntimeException(message);
    }
}
