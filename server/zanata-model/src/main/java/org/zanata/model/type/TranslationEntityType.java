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

import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.tm.TransMemoryUnit;
import java.io.Serializable;

/**
 * Entity type of origin for translation copied.
 *
 * Usage {@link org.zanata.model.HTextFlowTarget.entityType} and
 * {@link org.zanata.model.HTextFlowTargetHistory.entityType}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public enum TranslationEntityType implements Serializable {
    TMX(TransMemoryUnit.class),
    TFT(HTextFlowTarget.class),
    TTH(HTextFlowTargetHistory.class);
    // MT(null);
    private final Class clazz;

    private TranslationEntityType(Class clazz) {
        this.clazz = clazz;
    }

    public Class getClazz() {
        return this.clazz;
    }
}
