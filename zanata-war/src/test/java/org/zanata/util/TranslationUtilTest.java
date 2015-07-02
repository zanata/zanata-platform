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
import org.zanata.model.type.TranslationEntityType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslationUtilTest {

    @Test
    public void getEntityIdNullTarget() {
        assertThat(TranslationUtil.getEntityId(null)).isNull();
    }

    @Test
    public void getEntityIdNullId() {
        Long id = 10L;
        Long entityId = null;

        HTextFlowTarget target = generateTarget(id, entityId);
        assertThat(TranslationUtil.getEntityId(target)).isEqualTo(id);
    }

    @Test
    public void getEntityId() {
        Long id = 10L;
        Long entityId = 1L;

        HTextFlowTarget target = generateTarget(id, entityId);
        assertThat(TranslationUtil.getEntityId(target)).isEqualTo(entityId);
    }

    @Test
    public void getEntityTypeNullTarget() {
        assertThat(TranslationUtil.getEntityType(null)).isNull();
    }

    @Test
    public void getEntityTypeNullType() {
        Long id = 10L;
        Long entityId = 1L;
        TranslationEntityType entityType = null;

        HTextFlowTarget target = generateTarget(id, entityId, entityType);
        assertThat(TranslationUtil.getEntityType(target)).isEqualTo(
            TranslationEntityType.TFT);
    }

    @Test
    public void getEntityType() {
        Long id = 10L;
        Long entityId = 1L;
        TranslationEntityType entityType = TranslationEntityType.TMX;

        HTextFlowTarget target = generateTarget(id, entityId, entityType);
        assertThat(TranslationUtil.getEntityType(target)).isEqualTo(
            entityType);
    }

    @Test
    public void getEntityTypeWithNullTarget() {
        Long id = 10L;
        Long entityId = 1L;

        HTextFlowTarget target = generateTarget(id, entityId, null);
        assertThat(TranslationUtil.getEntityType(target)).isEqualTo(
            TranslationEntityType.TFT);
    }

    @Test
    public void getEntityTypeWithNullTargetHistory() {
        Long entityId = 1L;

        HTextFlowTargetHistory history = generateTargetHistory(entityId, null);
        assertThat(TranslationUtil.getEntityType(history)).isEqualTo(
            TranslationEntityType.TTH);
    }

    @Test
    public void copyHTextFlowTargetEntity() {
        Long id = 10L;
        Long entityId = 1L;
        TranslationEntityType entityType = TranslationEntityType.TMX;

        HTextFlowTarget from = generateTarget(id, entityId, entityType);
        HTextFlowTarget to = new HTextFlowTarget();

        TranslationUtil.copyEntity(from, to);
        assertThat(to.getEntityId()).isEqualTo(entityId);
        assertThat(to.getEntityType()).isEqualTo(entityType);
    }

    public HTextFlowTarget generateTarget(Long id, Long entityId) {
        HTextFlowTarget target = new HTextFlowTarget();
        target.setEntityId(entityId);
        target.setId(id);
        return target;
    }

    public HTextFlowTarget generateTarget(Long id, Long entityId,
            TranslationEntityType entityType) {
        HTextFlowTarget target = generateTarget(id, entityId);
        target.setEntityType(entityType);
        return target;
    }

    public HTextFlowTargetHistory generateTargetHistory(Long entityId,
        TranslationEntityType entityType) {
        HTextFlowTargetHistory history = new HTextFlowTargetHistory();
        history.setEntityId(entityId);
        history.setEntityType(entityType);
        return history;
    }
}
