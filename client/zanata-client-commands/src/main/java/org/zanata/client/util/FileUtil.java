/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

package org.zanata.client.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * guava Files is still under Beta and findbugs will reject it. This class is
 * just so we can reuse some of the stuff in there.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FileUtil {

    /**
     * This is taken from guava Files.
     * Returns the lexically cleaned form of the path name, <i>usually</i> (but
     * not always) equivalent to the original. The following heuristics are used:
     *
     * <ul>
     * <li>empty string becomes .
     * <li>. stays as .
     * <li>fold out ./
     * <li>fold out ../ when possible
     * <li>collapse multiple slashes
     * <li>delete trailing slashes (unless the path is just "/")
     * </ul>
     *
     * <p>These heuristics do not always match the behavior of the filesystem. In
     * particular, consider the path {@code a/../b}, which {@code simplifyPath}
     * will change to {@code b}. If {@code a} is a symlink to {@code x}, {@code
     * a/../b} may refer to a sibling of {@code x}, rather than the sibling of
     * {@code a} referred to by {@code b}.
     */
    public static String simplifyPath(String pathname) {
        checkNotNull(pathname);
        if (pathname.length() == 0) {
            return ".";
        }

        // split the path apart
        Iterable<String> components =
                Splitter.on('/').omitEmptyStrings().split(pathname);
        List<String> path = new ArrayList<String>();

        // resolve ., .., and //
        for (String component : components) {
            if (component.equals(".")) {
                continue;
            } else if (component.equals("..")) {
                if (path.size() > 0 && !path.get(path.size() - 1).equals("..")) {
                    path.remove(path.size() - 1);
                } else {
                    path.add("..");
                }
            } else {
                path.add(component);
            }
        }

        // put it back together
        String result = Joiner.on('/').join(path);
        if (pathname.charAt(0) == '/') {
            result = "/" + result;
        }

        while (result.startsWith("/../")) {
            result = result.substring(3);
        }
        if (result.equals("/..")) {
            result = "/";
        } else if ("".equals(result)) {
            result = ".";
        }

        return result;
    }
}
