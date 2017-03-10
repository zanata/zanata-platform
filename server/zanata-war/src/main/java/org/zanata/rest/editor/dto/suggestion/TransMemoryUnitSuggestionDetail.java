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

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.model.tm.TransMemoryUnit;
import java.util.Date;

/**
 * Detailed information about a suggestion from an imported translation memory.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TransMemoryUnitSuggestionDetail implements SuggestionDetail {
    private final SuggestionType type = SuggestionType.IMPORTED_TM;

    /**
     * The database id that can be used to look up the TransMemoryUnit.
     */
    private final Long transMemoryUnitId;
    private final String transMemorySlug;
    private final String transUnitId;
    @JsonSerialize(using = JsonDateSerializer.class)
    private final Date lastChanged;

    /**
     * Create a detail object based on a given trans memory unit.
     *
     * @param tmUnit
     *            for which to create a detail object
     */
    public TransMemoryUnitSuggestionDetail(TransMemoryUnit tmUnit) {
        this.transMemoryUnitId = tmUnit.getId();
        this.transMemorySlug = tmUnit.getTranslationMemory().getSlug();
        this.transUnitId = tmUnit.getTransUnitId();
        this.lastChanged = tmUnit.getLastChanged();
    }

    public SuggestionType getType() {
        return this.type;
    }

    /**
     * The database id that can be used to look up the TransMemoryUnit.
     */
    public Long getTransMemoryUnitId() {
        return this.transMemoryUnitId;
    }

    public String getTransMemorySlug() {
        return this.transMemorySlug;
    }

    public String getTransUnitId() {
        return this.transUnitId;
    }

    public Date getLastChanged() {
        return this.lastChanged;
    }
}
