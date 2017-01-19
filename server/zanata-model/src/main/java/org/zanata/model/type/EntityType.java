/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.tm.TransMemoryUnit;

/**
 * Entity type with abbreviation.
 *
 * Usage: {@link org.zanata.model.HTextFlowTarget.copiedEntityType}
 * {@link org.zanata.model.HTextFlowTargetHistory.copiedEntityType}
 * {@link org.zanata.model.Activity.lastTargetType}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public enum EntityType {
    HTexFlowTarget(HTextFlowTarget.class, "TFT"),
    HTextFlowTargetHistory(HTextFlowTargetHistory.class, "TTH"),
    HProjectIteration(HProjectIteration.class, "VER"),
    HDocument(HDocument.class, "DOC"),
    TMX(TransMemoryUnit.class, "TMX");
    private final Class<?> entityClass;
    private final String abbr;

    EntityType(Class<?> entityClass, String abbr) {
        this.entityClass = entityClass;
        this.abbr = abbr;
    }

    public static EntityType getValueOf(String abbr) {
        switch (abbr) {
        case "TFT":
            return EntityType.HTexFlowTarget;

        case "TTH":
            return EntityType.HTextFlowTargetHistory;

        case "VER":
            return EntityType.HProjectIteration;

        case "DOC":
            return EntityType.HDocument;

        case "TMX":
            return EntityType.TMX;

        default:
            throw new IllegalArgumentException(String.valueOf(abbr));

        }
    }

    public Class<?> getEntityClass() {
        return this.entityClass;
    }

    public String getAbbr() {
        return this.abbr;
    }
}
