/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.shared.util;

import com.allen_sauer.gwt.log.client.Log;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TokenUtil {
    /**
     *
     * @see org.zanata.webtrans.client.history.Token#decode(String)
     * @param toEncode
     * @return the given string with all token delimiters encoded
     */
    public static String encode(String toEncode) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < toEncode.length(); i++) {
            char nextChar = toEncode.charAt(i);
            switch (nextChar) {
                case ':':
                    sb.append("!c");
                    break;
                case ';':
                    sb.append("!s");
                    break;
                case '!':
                    sb.append('!');
                default:
                    sb.append(nextChar);
            }
        }
        // Log.debug("Encoded: \"" + toEncode + "\" to \"" + sb + "\"");
        return sb.toString();
    }

    /**
     * @see #encode(String)
     * @param toDecode
     * @return the given string with any encoded token delimiters decoded
     */
    public static String decode(String toDecode) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < toDecode.length(); i++) {
            char nextChar = toDecode.charAt(i);
            if (escaped) {
                escaped = false;
                switch (nextChar) {
                    case '!':
                        sb.append('!');
                        break;
                    case 'c':
                        sb.append(':');
                        break;
                    case 's':
                        sb.append(';');
                        break;
                    default:
                        Log.warn("Unrecognised escaped character, appending: "
                            + nextChar);
                        sb.append(nextChar);
                }
            } else if (nextChar == '!') {
                escaped = true;
            } else {
                sb.append(nextChar);
            }
        }
        Log.debug("Decoded: \"" + toDecode + "\" to \"" + sb + "\"");
        return sb.toString();
    }
}
