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
package org.zanata.common;

import java.io.Serializable;

import javax.annotation.ParametersAreNonnullByDefault;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ParametersAreNonnullByDefault
public class FileTypeName implements Comparable<FileTypeName>, Serializable {
    private static final long serialVersionUID = 703862390590961467L;
    /** Name of the FileType/DocumentType. NS: This may not correspond to a
     * FileType/DocumentType recognised by the client (eg if server is newer).
     * @see DocumentType
     */
    private final String name;

    public FileTypeName(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(FileTypeName o) {
        return name.compareTo(o.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileTypeName) {
            FileTypeName o = (FileTypeName) obj;
            return name.equals(o.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
