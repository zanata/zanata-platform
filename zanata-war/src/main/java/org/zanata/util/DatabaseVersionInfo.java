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

import com.google.common.base.MoreObjects;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class DatabaseVersionInfo {
    private final String name;
    private final int majorVersion;
    private final int minorVersion;
    private final String version;

    public DatabaseVersionInfo(String name, int majorVersion,
            int minorVersion,
            String version) {
        this.name = name;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.version = version;
    }

    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("name", name)
                .add("version", version)
                .add("majorVer", majorVersion)
                .add("minorVer", minorVersion)
                .toString();
    }
}
