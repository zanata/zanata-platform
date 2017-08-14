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

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * NB: DocumentType should only be used by Zanata Server, not client code.
 * Represents a file type supported by Zanata, with an associated list of file extensions.
 * Note that the list of file types and default extensions, as supported by a
 * given Zanata server, may be different from the list in this enum.
 * <p>
 *     Zanata's FileResource REST service has a function fileTypeInfoList(),
 *     which returns information about which file types are actually supported
 *     by the server.
 * </p>
 * @see FileTypeInfo
 */
// TODO move this to server, not api
public enum DocumentType {

    GETTEXT(buildPotMap()),
    PLAIN_TEXT("txt"), XML_DOCUMENT_TYPE_DEFINITION("dtd"),

    OPEN_DOCUMENT_TEXT("odt"), OPEN_DOCUMENT_TEXT_FLAT("fodt"),
    OPEN_DOCUMENT_PRESENTATION("odp"), OPEN_DOCUMENT_PRESENTATION_FLAT("fodp"),
    OPEN_DOCUMENT_SPREADSHEET("ods"), OPEN_DOCUMENT_SPREADSHEET_FLAT("fods"),
    OPEN_DOCUMENT_GRAPHICS("odg"), OPEN_DOCUMENT_GRAPHICS_FLAT("fodg"),
    OPEN_DOCUMENT_DATABASE("odb"), OPEN_DOCUMENT_FORMULA("odf"),

    HTML("html", "htm"), IDML("idml"),

    SUBTITLE("srt", "sbt", "sub", "vtt"),
    PROPERTIES("properties"), PROPERTIES_UTF8("properties"),
    XML("xml"), XLIFF("xlf"), TS("ts"), JSON("json");

    private static final Set<String> allSourceExtensions = buildExtensionsList(true);

    private static final Set<String> allTranslationExtensions = buildExtensionsList(false);

    private static Map<String, String> buildPotMap() {
        Map<String, String> potMap = new HashMap<>();
        potMap.put("pot", "po");
        return unmodifiableMap(potMap);
    }

    public FileTypeInfo toFileTypeInfo() {
        return new FileTypeInfo(new FileTypeName(this.name()), this.extensions);
    }

    private static Set<String> buildExtensionsList(boolean source) {
        Set<String> allExtensions = new HashSet<>();
        for (DocumentType type : DocumentType.values()) {
            if (source) {
                allExtensions.addAll(type.getSourceExtensions());
            } else {
                allExtensions.addAll(type.getTranslationExtensions());
            }
        }
        return unmodifiableSet(allExtensions);
    }

    public static @Nullable DocumentType getByName(@Nullable String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return DocumentType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * @return a read-only list of source file extensions for known file types
     */
    public static Set<String> getAllSourceExtensions() {
        return allSourceExtensions;
    }

    /**
     * @return a read-only list of translation file extensions for known file types
     */
    public static Set<String> getAllTranslationExtensions() {
        return allTranslationExtensions;
    }

    public static Set<DocumentType> fromSourceExtension(String sourceExtension) {
        return fromExtension(true, sourceExtension);
    }

    public static Set<DocumentType> fromTranslationExtension(
            String translationExtension) {
        return fromExtension(false, translationExtension);
    }

    private static Set<DocumentType> fromExtension(boolean source,
            String extension) {
        Set<DocumentType> documentTypes = new HashSet<>();
        for (DocumentType type : DocumentType.values()) {
            Collection<String> extensions =
                    source ? type.extensions.keySet() : type.extensions
                            .values();

            if (extensions.contains(extension)) {
                documentTypes.add(type);
            }
        }
        return documentTypes;
    }


    private final Map<String, String> extensions;

    /**
     * Create a document type enum constant with the given list of default extensions.
     */
    // Don't use @Nonnull in the constructor signature or you'll cause
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=388314 and
    // https://bugs.openjdk.java.net/browse/JDK-8024694
    DocumentType(String... extensions) throws IllegalArgumentException {
        Map<String, String> extensionsMap = new HashMap<>();
        for (String extension : extensions) {
            extensionsMap.put(extension, extension);
        }
        this.extensions = unmodifiableMap(extensionsMap);
    }

    /**
     * Create a document type enum constant with the given list of default extensions.
     */
    // Don't use @Nonnull in the constructor signature or you'll cause
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=388314 and
    // https://bugs.openjdk.java.net/browse/JDK-8024694
    DocumentType(Map<String, String> extensions) throws IllegalArgumentException {
        this.extensions = unmodifiableMap(extensions);
    }

    public Map<String, String> getExtensions() {
        return extensions;
    }

    public Set<String> getSourceExtensions() {
        return unmodifiableSet(new HashSet<>(extensions.keySet()));
    }

    public Set<String> getTranslationExtensions() {
        return unmodifiableSet(new HashSet<>(extensions.values()));
    }
}
