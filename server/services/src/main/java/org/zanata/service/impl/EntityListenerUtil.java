/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import org.hibernate.event.spi.PostUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
final class EntityListenerUtil {
    private static final Logger log =
            LoggerFactory.getLogger(EntityListenerUtil.class);
    /**
     * Try to locate index for field in the entity. We try to optimize a
     * bit here since the index should be consistent and only need to be looked
     * up once. If the given index is not null, it means it has been looked up
     * and set already so we just return that value. Otherwise it will look it
     * up in hibernate persister and return the index value.
     *
     * @param slugFieldIndex
     *            if not null it will be the index to use
     * @param event
     *            post update event for an entity
     * @param entityField field name to look up in entity
     * @return looked up index for a given field for the entity
     */
    static int getFieldIndex(Integer slugFieldIndex,
            PostUpdateEvent event, String entityField) {
        if (slugFieldIndex != null) {
            return slugFieldIndex;
        }
        String[] propertyNames = event.getPersister().getPropertyNames();
        int i;
        for (i = 0; i < propertyNames.length; i++) {
            String propertyName = propertyNames[i];
            if (propertyName.equals(entityField)) {
                return i;
            }
        }
        log.error("can not find {} index in entity [{}] properties [{}]",
                entityField, event.getEntity(),
                Lists.newArrayList(propertyNames));
        throw new IllegalStateException(
                "can not find " + entityField + " index in entity properties");
    }
}
