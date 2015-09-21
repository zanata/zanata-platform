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

package org.zanata.model.type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;

/**
 * Source of action on how translation are being copied.
 *
 * Usage {@link org.zanata.model.HTextFlowTarget.sourceType} and
 * {@link org.zanata.model.HTextFlowTargetHistory.sourceType}
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

    public static final Collection<TranslationSourceType> AUTOMATED_ENTRIES;

    static {
        AUTOMATED_ENTRIES =
                ImmutableSet.of(TranslationSourceType.COPY_TRANS,
                        TranslationSourceType.COPY_VERSION,
                        TranslationSourceType.MERGE_VERSION,
                        TranslationSourceType.TM_MERGE);
    }

    @Getter
    private final String abbr;

    private static final Map<String, TranslationSourceType> allAbbreviations =
            Maps.newHashMap();

    /* static initializer */
    static {
        for (TranslationSourceType enumVal : values()) {
            allAbbreviations.put(enumVal.getAbbr(), enumVal);
        }
    }

    TranslationSourceType(String abbr) {
        this.abbr = abbr;
    }

    public static TranslationSourceType getValueOf(String abbr) {
        TranslationSourceType sourceType = allAbbreviations.get(abbr);
        if (sourceType == null) {
            throw new IllegalArgumentException("'" + abbr + "'");
        } else {
            return sourceType;
        }
    }

    public boolean isAutomatedEntry() {
        return AUTOMATED_ENTRIES.contains(this);
    }
}
