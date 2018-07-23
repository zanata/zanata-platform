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
package org.zanata.rest.editor.dto.suggestion;

import java.io.Serializable;

/**
 * Detailed information about a suggestion of a specific type.
 */
public interface SuggestionDetail extends Serializable {

    /**
     * Possible types of suggestions from different resources.
     *
     * Different types may present different information, so use
     * different class representations.
     */
    class SuggestionType implements Serializable {

        /**
         * A suggestion from a project on this Zanata server.
         */
        public static final SuggestionType LOCAL_PROJECT =
            new SuggestionType("LOCAL_PROJECT", null);

        /**
         * A suggestion from an imported translation memory.
         */
        public static final SuggestionType IMPORTED_TM =
            new SuggestionType("IMPORTED_TM", null);

        private String key;
        private String metadata;

        SuggestionType(String key, String metadata) {
            this.key = key;
            this.metadata = metadata;
        }

        public String getKey() {
            return key;
        }

        /**
         * @return metadata for the suggestion type.
         * For MT, metadata is used to store the backendId
         */
        public String getMetadata() {
            return metadata;
        }
    }

    /**
     * @return the type of suggestion.
     */
    SuggestionType getType();
}
