/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class GlossarySortField implements Serializable {

    public static final String SRC_CONTENT = "src_content";
    public static final String PART_OF_SPEECH = "part_of_speech";
    public static final String TRANS_COUNT = "trans_count";
    public static final String DESCRIPTION = "desc";

    private static final Map<String, String> fieldMap;
    static
    {
        fieldMap = new HashMap<String, String>();
        fieldMap.put(SRC_CONTENT, "term.content");
        fieldMap.put(PART_OF_SPEECH, "term.glossaryEntry.pos");
        fieldMap.put(TRANS_COUNT, "size(term.glossaryEntry.glossaryTerms)");
        fieldMap.put(DESCRIPTION, "term.glossaryEntry.description");
    }

    private final String entityField;
    private final boolean ascending;

    public GlossarySortField(String entityField, boolean ascending) {
        this.entityField = entityField;
        this.ascending = ascending;
    }

    public String getEntityField() {
        return entityField;
    }

    public boolean isAscending() {
        return ascending;
    }

    public static final GlossarySortField getByField(String field) {
        if (field == null || field.length() <= 0) {
            throw new IllegalArgumentException(field);
        }

        boolean isAscending = !field.startsWith("-");
        String processedField =
            field.startsWith("-") ? field.substring(1) : field;

        if (fieldMap.containsKey(processedField)) {
            return new GlossarySortField(fieldMap.get(processedField),
                    isAscending);
        }
        return null;
    }
}
