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

package org.zanata.dao;

import java.util.Date;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.webtrans.shared.model.TransUnitId;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = { "jpa-tests" })
public class TextFlowTargetReviewCommentsDAOJPATest extends ZanataDbunitJpaTest {
    private TextFlowTargetReviewCommentsDAO reviewCommentsDAO;
    private TextFlowTargetDAO textFlowTargetDAO;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
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

    @BeforeMethod(firstTimeOnly = true)
    public void setup() {
        reviewCommentsDAO = new TextFlowTargetReviewCommentsDAO(getSession());
        textFlowTargetDAO = new TextFlowTargetDAO(getSession());
    }

    @Test
    public void testQuery() {
        List<HTextFlowTargetReviewComment> reviewComments =
                reviewCommentsDAO.getReviewComments(new TransUnitId(5L),
                        LocaleId.EN_US);

        assertThat(reviewComments, Matchers.hasSize(1));
        assertThat(reviewComments.get(0).getCommenter().getName(),
                Matchers.equalTo("Sample User"));
    }

    @Test
    public void testTargetUserComment() {
        PersonDAO personDAO = new PersonDAO(getSession());
        HPerson person = personDAO.findById(1L, false);
        HTextFlowTarget target = textFlowTargetDAO.findById(1L, false);

        List<HTextFlowTargetReviewComment> userComments =
                target.getReviewComments();

        assertThat(userComments, Matchers.empty());

        target.addReviewComment("bad translation", person);
        getEm().persist(target);

        List<HTextFlowTargetReviewComment> result =
                reviewCommentsDAO.getReviewComments(new TransUnitId(target
                        .getTextFlow().getId()), target.getLocaleId());

        assertThat(result, Matchers.hasSize(1));
        assertThat(result.get(0).getCommenterName(),
                Matchers.equalTo(person.getName()));
        assertThat(result.get(0).getCreationDate(),
                Matchers.lessThanOrEqualTo(new Date()));
    }

    @Test
    public void testTargetUserCommentMadeOnPreviousTranslation() {
        PersonDAO personDAO = new PersonDAO(getSession());
        HPerson person = personDAO.findById(1L, false);
        HTextFlowTarget target = textFlowTargetDAO.findById(2L, false);

        List<String> oldTranslation = target.getContents();
        int oldVersion = target.getVersionNum();

        target.addReviewComment("comment blah", person);
        getEm().persist(target);

        // change target after making comment
        target.setContent0("new translation");
        getEm().persist(target);

        List<HTextFlowTargetReviewComment> result =
                reviewCommentsDAO.getReviewComments(new TransUnitId(target
                        .getTextFlow().getId()), target.getLocaleId());

        assertThat(result.get(0).getTargetVersion(),
                Matchers.equalTo(oldVersion));
    }
}
