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

package org.zanata.util;

import org.junit.Test;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.type.EntityType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslationUtilTest {

    @Test
    public void getEntityIdNullTarget() {
        assertThat(TranslationUtil.getCopiedEntityId(null)).isNull();
    }

    @Test
    public void getEntityIdNullId() {
        Long id = 10L;
        Long entityId = null;

        HTextFlowTarget target = generateTarget(id, entityId);
        assertThat(TranslationUtil.getCopiedEntityId(target)).isEqualTo(id);
    }

    @Test
    public void getEntityId() {
        Long id = 10L;
        Long entityId = 1L;

        HTextFlowTarget target = generateTarget(id, entityId);
        assertThat(TranslationUtil.getCopiedEntityId(target)).isEqualTo(entityId);
    }

    @Test
    public void getEntityTypeNullTarget() {
        assertThat(TranslationUtil.getCopiedEntityType(null)).isNull();
    }

    @Test
    public void getEntityTypeNullType() {
        Long id = 10L;
        Long entityId = 1L;
        EntityType entityType = null;

        HTextFlowTarget target = generateTarget(id, entityId, entityType);
        assertThat(TranslationUtil.getCopiedEntityType(target)).isEqualTo(
            EntityType.HTexFlowTarget);
    }

    @Test
    public void getEntityType() {
        Long id = 10L;
        Long entityId = 1L;
        EntityType entityType = EntityType.TMX;

        HTextFlowTarget target = generateTarget(id, entityId, entityType);
        assertThat(TranslationUtil.getCopiedEntityType(target)).isEqualTo(
            entityType);
    }

    @Test
    public void getEntityTypeWithNullTarget() {
        Long id = 10L;
        Long entityId = 1L;

        HTextFlowTarget target = generateTarget(id, entityId, null);
        assertThat(TranslationUtil.getCopiedEntityType(target)).isEqualTo(
            EntityType.HTexFlowTarget);
    }

    @Test
    public void getEntityTypeWithNullTargetHistory() {
        Long entityId = 1L;

        HTextFlowTargetHistory history = generateTargetHistory(entityId, null);
        assertThat(TranslationUtil.getCopiedEntityType(history)).isEqualTo(
            EntityType.HTextFlowTargetHistory);
    }

    @Test
    public void copyHTextFlowTargetEntity() {
        Long id = 10L;
        Long entityId = 1L;
        EntityType entityType = EntityType.TMX;

        HTextFlowTarget from = generateTarget(id, entityId, entityType);
        HTextFlowTarget to = new HTextFlowTarget();

        TranslationUtil.copyEntity(from, to);
        assertThat(to.getCopiedEntityId()).isEqualTo(entityId);
        assertThat(to.getCopiedEntityType()).isEqualTo(entityType);
    }

    public HTextFlowTarget generateTarget(Long id, Long entityId) {
        HTextFlowTarget target = new HTextFlowTarget();
        target.setCopiedEntityId(entityId);
        target.setId(id);
        return target;
    }

    public HTextFlowTarget generateTarget(Long id, Long entityId,
        EntityType entityType) {
        HTextFlowTarget target = generateTarget(id, entityId);
        target.setCopiedEntityType(entityType);
        return target;
    }

    public HTextFlowTargetHistory generateTargetHistory(Long entityId,
        EntityType entityType) {
        HTextFlowTargetHistory history = new HTextFlowTargetHistory();
        history.setCopiedEntityId(entityId);
        history.setCopiedEntityType(entityType);
        return history;
    }
}
