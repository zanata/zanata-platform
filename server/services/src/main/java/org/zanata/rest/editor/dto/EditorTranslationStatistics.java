/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
 *  @author tags. See the copyright.txt file in the distribution for a full
 *  listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */

package org.zanata.rest.editor.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.rest.dto.stats.TranslationStatistics;

import java.util.Objects;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EditorTranslationStatistics extends TranslationStatistics {
    private int mt;

    public EditorTranslationStatistics() {
        super();
    }

    public static EditorTranslationStatistics getInstance(
        TranslationStatistics from) {
        EditorTranslationStatistics statistics =
            new EditorTranslationStatistics();
        statistics.setLastTranslated(from.getLastTranslated());
        statistics.setLastTranslatedBy(from.getLastTranslatedBy());
        statistics.setLastTranslatedDate(from.getLastTranslatedDate());
        statistics.setLocale(from.getLocale());
        statistics.setUnit(from.getUnit());
        statistics.setApproved(from.getApproved());
        statistics.setFuzzy(from.getFuzzy());
        statistics.setRejected(from.getRejected());
        statistics.setTranslatedOnly(from.getTranslatedOnly());
        statistics.setUntranslated(from.getUntranslated());
        return statistics;
    }

    public int getMt() {
        return mt;
    }

    public void setMt(int mt) {
        this.mt = mt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EditorTranslationStatistics)) return false;
        EditorTranslationStatistics that = (EditorTranslationStatistics) o;
        return mt == that.mt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mt);
    }
}
