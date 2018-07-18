/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.Collection;
import com.google.common.collect.ImmutableSet;

/**
 * INTERNAL API ONLY - SUBJECT TO CHANGE OR REMOVAL WITHOUT NOTICE <br/>
 *
 * Source of action on how translation are being copied.
 *
 * Usage {@link org.zanata.model.HTextFlowTarget#sourceType} and
 * {@link org.zanata.model.HTextFlowTargetHistory#sourceType}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public enum TranslationSourceType implements Serializable {
    COPY_TRANS("CT"),
    COPY_VERSION("CV"),
    MERGE_VERSION("MV"),
    TM_MERGE("TM"),
    MACHINE_TRANS("MT"),
    GWT_EDITOR_ENTRY("GWT"),
    JS_EDITOR_ENTRY("JS"),
    API_UPLOAD("API"),
    WEB_UPLOAD("WEB"),
    UNKNOWN("UNK");
    public static final Collection<TranslationSourceType> AUTOMATED_ENTRIES = ImmutableSet.of(
            COPY_TRANS,
            COPY_VERSION,
            MACHINE_TRANS,
            MERGE_VERSION,
            TM_MERGE);

    private final String abbr;

    TranslationSourceType(String abbr) {
        this.abbr = abbr;
    }

    public static TranslationSourceType getValueOf(String abbr) {
        switch (abbr) {
        case "CT":
            return COPY_TRANS;

        case "CV":
            return COPY_VERSION;

        case "MV":
            return MERGE_VERSION;

        case "TM":
            return TM_MERGE;

        case "GWT":
            return GWT_EDITOR_ENTRY;

        case "JS":
            return JS_EDITOR_ENTRY;

        case "API":
            return API_UPLOAD;

        case "WEB":
            return WEB_UPLOAD;

        case "MT":
            return MACHINE_TRANS;

        case "UNK":
            return UNKNOWN;

        default:
            throw new IllegalArgumentException(String.valueOf(abbr));

        }
    }

    public boolean isAutomatedEntry() {
        return AUTOMATED_ENTRIES.contains(this);
    }

    public String getAbbr() {
        return this.abbr;
    }
}
