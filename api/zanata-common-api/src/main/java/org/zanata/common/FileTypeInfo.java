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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ParametersAreNonnullByDefault
public class FileTypeInfo implements Serializable {
    /** Name of the DocumentType. NS: This may not correspond to a DocumentType
     * recognised by the client (eg if server is newer).
     * @see DocumentType
     */
    private final FileTypeName type;
    /**
     * Default file extensions for this type. Keys are source extensions, values are target extensions.
     */
    private final Map<String, String> extensions;

    public FileTypeInfo(
            @JsonProperty("type") FileTypeName type,
            @JsonProperty("extensions") Map<String, String> extensions) {
        this.type = type;
        this.extensions = unmodifiableMap(extensions);
    }

    public FileTypeName getType() {
        return type;
    }

    public Map<String, String> getExtensions() {
        return extensions;
    }

    @JsonIgnore
    public Collection<String> getSourceExtensions() {
        return unmodifiableSet(extensions.keySet());
    }

    @JsonIgnore
    public Collection<String> getTranslationExtensions() {
        return unmodifiableCollection(extensions.values());
    }

    public FileTypeInfo withExtensions(Map<String, String> extensions) {
        return new FileTypeInfo(type, extensions);
    }

}
