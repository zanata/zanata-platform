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

package org.zanata.client.commands;

import org.apache.commons.io.FilenameUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Represents document name with extension.
 */
public class QualifiedSrcDocName {
    private final String fullName;
    private final String extension;

    QualifiedSrcDocName(String fullName) {
        this.fullName = fullName;
        extension = FilenameUtils.getExtension(fullName).toLowerCase();
    }
    public static QualifiedSrcDocName from(String qualifiedName) {
        String extension = FilenameUtils.getExtension(qualifiedName);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(extension),
                "expect a qualified document name (with extension)");
        return new QualifiedSrcDocName(qualifiedName);
    }
    public static QualifiedSrcDocName from(String unqualifiedName, String extension) {
        return new QualifiedSrcDocName(unqualifiedName + "." + extension);
    }

    public String getFullName() {
        return fullName;
    }

    public String getExtension() {
        return extension;
    }
}
