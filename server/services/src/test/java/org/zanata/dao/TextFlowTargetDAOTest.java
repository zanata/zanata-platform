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

package org.zanata.dao;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.type.EntityType;
import org.zanata.model.type.TranslationSourceType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TextFlowTargetDAOTest extends ZanataDbunitJpaTest {

    private TextFlowTargetDAO textFlowTargetDAO;

    @Before
    public void setup() {
        textFlowTargetDAO = new TextFlowTargetDAO(getSession());
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/ClearAllTables.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/AccountData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/ProjectsData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/TextFlowTestData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
            "org/zanata/test/model/LocalesData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    public void entityTypeAndSourceTypeTest() {
        //get target with no history, revision = 0
        HTextFlowTarget target = textFlowTargetDAO.findById(1L);

        //create revision = 1
        EntityType entityType1 = EntityType.HTexFlowTarget;
        TranslationSourceType sourceType1 = TranslationSourceType.COPY_TRANS;
        Long copiedEntityId1 = 2L;

        target.setCopiedEntityType(entityType1);
        target.setSourceType(sourceType1);
        target.setCopiedEntityId(copiedEntityId1);

        target = textFlowTargetDAO.makePersistent(target);
        textFlowTargetDAO.flush();

        assertThat(target.getEntityType()).isEqualTo(entityType1);
        assertThat(target.getSourceType()).isEqualTo(sourceType1);
        assertThat(target.getCopiedEntityId()).isEqualTo(copiedEntityId1);

        //Create new revision = 2
        TranslationSourceType sourceType2 = TranslationSourceType.COPY_VERSION;

        target.setContents("new content");
        target.setSourceType(sourceType2);
        target.setCopiedEntityId(null);
        target.setCopiedEntityType(null);

        textFlowTargetDAO.makePersistent(target);
        textFlowTargetDAO.flush();

        target = textFlowTargetDAO.findById(1L);

        assertThat(target.getCopiedEntityType()).isEqualTo(null);
        assertThat(target.getCopiedEntityId()).isEqualTo(null);
        assertThat(target.getSourceType()).isEqualTo(sourceType2);

        HTextFlowTargetHistory history1 = target.getHistory().get(2);
        assertThat(history1.getCopiedEntityType()).isEqualTo(entityType1);
        assertThat(history1.getSourceType()).isEqualTo(sourceType1);
        assertThat(history1.getCopiedEntityId()).isEqualTo(copiedEntityId1);
    }
}
