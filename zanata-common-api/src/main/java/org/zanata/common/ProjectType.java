package org.zanata.common;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.zanata.common.DocumentType.*;

@XmlType(name = "projectTypeType")
@XmlEnum(String.class)
public enum ProjectType {
    Utf8Properties, Properties, Gettext, Podir, Xliff, Xml, File;

    private static final String OBSOLETE_PROJECT_TYPE_RAW = "raw";

    /**
     * @param projectType
     * @return
     * @throws Exception
     */
    public static ProjectType getValueOf(String projectType) throws Exception {
        if (projectType != null && !projectType.isEmpty()) {
            for (ProjectType pt : ProjectType.values()) {
                if (pt.name().equalsIgnoreCase(projectType)) {
                    return pt;
                }
            }
        }
        if (OBSOLETE_PROJECT_TYPE_RAW.equalsIgnoreCase(projectType)) {
            throw new Exception("Project type '" + projectType
                    + "' no longer supported, use 'File' instead");
        }
        throw new Exception("Project type '" + projectType + "' not supported");
    }

    public static List<String> getSupportedSourceFileTypes(ProjectType type) {
        if (type != null) {
            switch (type) {
                case Gettext:
                case Podir:
                    return GETTEXT_PORTABLE_OBJECT_TEMPLATE.getExtensions();
                case File:
                    return fileProjectSourceFileTypes();
                default:
                    break;
            }
        }
        return Arrays.asList();
    }

    private static List<String> fileProjectSourceFileTypes() {
        List<DocumentType> supported = Arrays.asList(XML_DOCUMENT_TYPE_DEFINITION,
                PLAIN_TEXT, IDML, HTML, OPEN_DOCUMENT_TEXT, OPEN_DOCUMENT_PRESENTATION,
                OPEN_DOCUMENT_GRAPHICS, SUBTITLE);

        List<String> extensions = new ArrayList<String>();
        for (DocumentType type : supported) {
            extensions.addAll(type.getExtensions());
        }
        return  extensions;
    }
}
