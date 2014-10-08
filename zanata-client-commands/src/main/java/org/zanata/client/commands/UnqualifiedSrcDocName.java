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
import org.zanata.common.ProjectType;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Represents document name without extension.
 */
public class UnqualifiedSrcDocName {
    private final String name;
    UnqualifiedSrcDocName(String name) {
        this.name = name;
    }
    public static UnqualifiedSrcDocName from(String docName) {
        String extension = FilenameUtils.getExtension(docName);
        Preconditions.checkArgument(Strings.isNullOrEmpty(extension),
                "expect an unqualified document name (without extension)");
        return new UnqualifiedSrcDocName(docName);
    }
    public QualifiedSrcDocName toQualifiedDocName(ProjectType projectType) {
        switch (projectType) {
            case Utf8Properties:
            case Properties:
                return QualifiedSrcDocName
                        .from(name, "properties");
            case Gettext:
            case Podir:
                return QualifiedSrcDocName.from(name, "pot");
            case Xliff:
            case Xml:
                return QualifiedSrcDocName.from(name, "xml");
            case File:
                throw new IllegalArgumentException("You can not using unqualified document name in file type project");
        }
        throw new IllegalStateException("Can not convert unqualified document name for this project type: " + projectType);
    }
}
