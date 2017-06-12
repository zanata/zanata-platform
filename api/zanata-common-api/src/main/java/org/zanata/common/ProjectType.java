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
package org.zanata.common;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.List;

import static org.zanata.common.DocumentType.*;

@XmlType(name = "projectTypeType")
@XmlEnum(String.class)
public enum ProjectType {
    Utf8Properties, Properties, Gettext, Podir, Xliff, Xml, File;

    private static final String OBSOLETE_PROJECT_TYPE_RAW = "raw";
    protected static final String PROJECT_TYPE_OFFLINE_PO = "offlinepo";

    /**
     * @param projectType
     * @return
     * @throws Exception
     */
    public static ProjectType getValueOf(String projectType) throws Exception {
        // TODO change all values to upper case and use ProjectType.valueOf(projectType)
        if (projectType == null || projectType.isEmpty()) {
            throw new Exception("No project type specified");
        }
        // handle offline po project type which is not part of the enum
        if (projectType.equalsIgnoreCase(PROJECT_TYPE_OFFLINE_PO)) {
            return ProjectType.Podir;
        }
        for (ProjectType pt : ProjectType.values()) {
            if (pt.name().equalsIgnoreCase(projectType)) {
                return pt;
            }
        }
        if (OBSOLETE_PROJECT_TYPE_RAW.equalsIgnoreCase(projectType)) {
            throw new Exception("Project type '" + projectType
                    + "' no longer supported, use 'File' instead");
        }
        throw new Exception("Project type '" + projectType + "' not supported");
    }

    /**
     * This method returns file types/extensions that is supported by web UI
     * upload.
     *
     * @param type
     *            project type
     * @return a list of file types or empty list if it's not a supported
     *         project type
     */
    @Deprecated
    public static List<DocumentType> getSupportedSourceFileTypes(ProjectType type) {
        if (type != null) {
            switch (type) {
                case Gettext:
                case Podir:
                    return Arrays.asList(GETTEXT);
                case File:
                    return fileProjectSourceDocTypes();
                default:
                    break;
            }
        }
        return Arrays.asList();
    }

    /**
     * @return source file types/extensions that this project type uses
     */
    @Deprecated
    public List<DocumentType> getSourceFileTypes() {
        switch (this) {
            case Utf8Properties:
            case Properties:
                return Arrays.asList(PROPERTIES);
            case Gettext:
            case Podir:
                return Arrays.asList(GETTEXT);
            case Xliff:
            case Xml:
                return Arrays.asList(XML);
            case File:
                return fileProjectSourceDocTypes();
            default:
                throw new IllegalStateException("unexpected value");
        }
    }

    /**
     * I think this list is meant to be similar or identical to TranslationFileServiceImpl.DOCTYPEMAP.keySet()
     * <p>
     * For some reason, this method returns a partial, hard-coded list of the
     * file types supported by the server, except these (as of 3.9):
     * OPEN_DOCUMENT_TEXT_FLAT
     * OPEN_DOCUMENT_PRESENTATION_FLAT
     * OPEN_DOCUMENT_SPREADSHEET_FLAT
     * OPEN_DOCUMENT_GRAPHICS_FLAT
     * OPEN_DOCUMENT_DATABASE
     * OPEN_DOCUMENT_FORMULA
     * XML
     * </p>
     * @return
     */
    @Deprecated
    public static List<DocumentType> fileProjectSourceDocTypes() {
        return Arrays.asList(XML_DOCUMENT_TYPE_DEFINITION,
            PLAIN_TEXT, IDML, HTML, OPEN_DOCUMENT_TEXT, OPEN_DOCUMENT_PRESENTATION,
            OPEN_DOCUMENT_GRAPHICS, OPEN_DOCUMENT_SPREADSHEET, SUBTITLE,
            GETTEXT, PROPERTIES, PROPERTIES_UTF8, XLIFF, TS, JSON);
    }
}
