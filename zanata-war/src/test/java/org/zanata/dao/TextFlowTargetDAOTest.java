/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.dao;

import java.util.Date;
import java.util.List;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.Matchers;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentState;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlowTargetReviewComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@Test(groups = { "jpa-tests" })
@Slf4j
public class TextFlowTargetDAOTest extends ZanataDbunitJpaTest
{

   private TextFlowTargetDAO textFlowTargetDAO;

   
   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.DELETE_ALL));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));      
   }
   
   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      textFlowTargetDAO = new TextFlowTargetDAO((Session) getEm().getDelegate());
   }
   
   // FIXME broken test
   @Test(enabled = false)
   public void findLatestEquivalentTranslation() throws Exception 
   {
      HDocument doc = (HDocument) getSession().get(HDocument.class, 1L);
      HLocale hLocale = (HLocale) getSession().get(HLocale.class, 1L);
      
      ScrollableResults results = this.textFlowTargetDAO.findMatchingTranslations(doc, hLocale, true, true, true, true);
      
      int rows = 0;
      
      // TODO Have more comprehensive results
      while(results.next())
      {
         final HTextFlowTarget oldTFT = (HTextFlowTarget)results.get(0);
         final HTextFlow textFlow = (HTextFlow)results.get(1);
         
         // make sure that each result is valid
         assertThat(oldTFT.getTextFlow(), equalTo(textFlow));
         assertThat(oldTFT.getLocale().getId(), equalTo(hLocale.getId()));
         assertThat(textFlow.getDocument().getDocId(), is("/my/path/document.txt"));
         assertThat(oldTFT.getState(), is(ContentState.Approved));
         
         rows++;
      }
      
      // make sure there where results
      assertThat(rows, greaterThan(0));
   }

   @Test
   public void testQuery()
   {
      HDocument doc = (HDocument) getSession().get(HDocument.class, 1L);
      HLocale hLocale = (HLocale) getSession().get(HLocale.class, 1L);
      @Cleanup
      ScrollableResults scroll = this.textFlowTargetDAO.findMatchingTranslations(doc, hLocale, true, true, true, true);
   }

   @Test
   public void testTargetUserComment()
   {
      PersonDAO personDAO = new PersonDAO(getSession());
      HPerson person = personDAO.findById(1L, false);
      HTextFlowTarget target = textFlowTargetDAO.findById(1L, false);

      List<HTextFlowTargetReviewComment> userComments = target.getReviewComments();

      assertThat(userComments, Matchers.empty());

      target.addUserComment("bad translation", person);
      getEm().persist(target);

      // @formatter:off
      HTextFlowTargetReviewComment result = getEm()
            .createQuery("from HTextFlowTargetReviewComment where comment = :comment", HTextFlowTargetReviewComment.class)
            .setParameter("comment", "bad translation").getSingleResult();
      // @formatter:on

      assertThat(result.getContentsOfCommentedTarget(), Matchers.equalTo(target.getContents()));
      assertThat(result.getCommenterName(), Matchers.equalTo(person.getName()));
      assertThat(result.getCreationDate(), Matchers.lessThanOrEqualTo(new Date()));
   }

   @Test
   public void testTargetUserCommentMadeOnPreviousTranslation()
   {
      PersonDAO personDAO = new PersonDAO(getSession());
      HPerson person = personDAO.findById(1L, false);
      HTextFlowTarget target = textFlowTargetDAO.findById(2L, false);

      List<String> oldTranslation = target.getContents();
      int oldVersion = target.getVersionNum();

      target.addUserComment("comment blah", person);
      getEm().persist(target);

      // change target after making comment
      target.setContent0("new translation");
      getEm().persist(target);

      // @formatter:off
      HTextFlowTargetReviewComment result = getEm()
            .createQuery("from HTextFlowTargetReviewComment where comment = :comment", HTextFlowTargetReviewComment.class)
            .setParameter("comment", "comment blah").getSingleResult();
      // @formatter:on

      assertThat(result.getContentsOfCommentedTarget(), Matchers.equalTo(oldTranslation));
      assertThat(result.getTargetVersion(), Matchers.equalTo(oldVersion));
   }
}
