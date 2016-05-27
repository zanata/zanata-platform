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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    XML("xml"), XLIFF("xml"), TS("ts");

    private static final Set<String> allSourceExtensions = buildExtensionsList(true);

    private static final Set<String> allTranslationExtensions = buildExtensionsList(false);

    private static Map<String, String> buildPotMap() {
        Map<String, String> potMap = new HashMap<>();
        potMap.put("pot", "po");
        return unmodifiableMap(potMap);
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

    public static @Nullable DocumentType getByName(String name) {
        try {
            return DocumentType.valueOf(name.toUpperCase());
        } catch (Exception e) {
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
     * Create a document type enum constant with the given list of extensions.
     * At least one extension must be specified.
     *
     * @throws IllegalArgumentException
     *             if no extensions are specified
     */
    DocumentType(@Nonnull String... extensions) throws IllegalArgumentException {
        if (extensions.length == 0) {
            throw new IllegalArgumentException(
                "DocumentType must be constructed with at least one extension.");
        }
        Map<String, String> extensionsMap = new HashMap<>();
        for (String extension : extensions) {
            extensionsMap.put(extension, extension);
        }
        this.extensions = unmodifiableMap(extensionsMap);
    }

    /**
     * Create a document type enum constant with the given list of extensions.
     * At least one extension must be specified.
     *
     * @throws IllegalArgumentException
     *             if no extensions are specified
     */
    DocumentType(@Nonnull Map<String, String> extensions) throws IllegalArgumentException {
        if (extensions.isEmpty()) {
            throw new IllegalArgumentException(
                    "DocumentType must be constructed with at least one extension.");
        }
        this.extensions = unmodifiableMap(extensions);
    }

    public Map<String, String> getExtensions() {
        return extensions;
    }

    public Set<String> getSourceExtensions() {
        return unmodifiableSet(new HashSet<>(extensions.keySet()));
    }

    public Set<String> getTranslationExtensions() {
        return unmodifiableSet(new HashSet(extensions.values()));
    }
}
