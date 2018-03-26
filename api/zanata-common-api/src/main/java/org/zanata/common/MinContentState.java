/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.common;

/**
 * @author Dragos Varovici <a
 *         href="mailto:dvarovici.work@gmail.com">dvarovici.work@gmail.com</a>
 */
public enum MinContentState {
    /** Approved only */
    Approved,
    /** Approved and Translated documents only */
    Translated;

    /**
     * Parse a MinContentState value from a string case-insensitively, and
     * disregarding leading and trailing spaces.
     *
     * @param str
     *            The string to parse.
     * @return The parsed MinContentState enum value, or null if the string did not
     *         match a value.
     */
    public static MinContentState fromString(String str) {
        MinContentState enumVal = null;

        if (str != null) {
            if (Approved.toString().equalsIgnoreCase(str.trim())) {
                enumVal = Approved;
            } else if (Translated.toString().equalsIgnoreCase(str.trim())) {
                enumVal = Translated;
            }
        }

        return enumVal;
    }
}
