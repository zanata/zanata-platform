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

import org.zanata.common.ProjectType;

/**
 * Represents a source document name without extension.
 */
public class DocNameWithoutExt {
    private final String name;
    DocNameWithoutExt(String name) {
        this.name = name;
    }

    public static DocNameWithoutExt from(String docName) {
        return new DocNameWithoutExt(docName);
    }

    public DocNameWithExt toDocNameWithExt(ProjectType projectType) {
        switch (projectType) {
            case Utf8Properties:
            case Properties:
                return DocNameWithExt
                        .from(name, "properties");
            case Gettext:
            case Podir:
                return DocNameWithExt.from(name, "pot");
            case Xliff:
            case Xml:
                return DocNameWithExt.from(name, "xml");
            case File:
                throw new IllegalArgumentException("You cannot use document name without extension with FILE project type");
        }
        throw new IllegalStateException("Cannot determine file extension for this project type: " + projectType);
    }
}
