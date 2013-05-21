/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service.impl;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.exception.ConcurrentTranslationException;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.TranslationService;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zanata.service.TranslationService.TranslationResult;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Test(groups = { "business-tests" })
public class TranslationServiceImplTest extends ZanataDbunitJpaTest
{
   private SeamAutowire seam = SeamAutowire.instance();

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void initializeSeam()
   {
      seam.reset()
          .use("entityManager", getEm())
          .use("session", getSession())
          .use(JpaIdentityStore.AUTHENTICATED_USER, seam.autowire(AccountDAO.class).getByUsername("demo"))
          .useImpl(LocaleServiceImpl.class)
          .ignoreNonResolvable();
   }

   @Test
   public void translate() throws Exception
   {
      TranslationService transService = seam.autowire(TranslationServiceImpl.class);

      TransUnitId transUnitId = new TransUnitId(1L);
      List<String> newContents = new ArrayList<String>(2);
      newContents.add("translated 1");
      newContents.add("translated 2");
      TransUnitUpdateRequest translateReq = new TransUnitUpdateRequest(transUnitId, newContents, ContentState.Approved, 1);

      List<TranslationResult> result = transService.translate(new LocaleId("de"), Lists.newArrayList(translateReq));

      assertThat(result.get(0).isTranslationSuccessful(), is(true));
      assertThat(result.get(0).getBaseVersionNum(), is(1));
      assertThat(result.get(0).getBaseContentState(), is(ContentState.Approved));
      assertThat(result.get(0).getTranslatedTextFlowTarget().getVersionNum(), is(2)); // moved up a version
   }

   @Test
   public void translateMultiple() throws Exception
   {
      TranslationService transService = seam.autowire(TranslationServiceImpl.class);

      List<TransUnitUpdateRequest> translationReqs = new ArrayList<TransUnitUpdateRequest>();

      // Request 1
      TransUnitId transUnitId = new TransUnitId(1L);
      List<String> newContents = new ArrayList<String>(2);
      newContents.add("translated 1");
      newContents.add("translated 2");
      translationReqs.add( new TransUnitUpdateRequest(transUnitId, newContents, ContentState.Approved, 1) );

      // Request 2 (different documents)
      transUnitId = new TransUnitId(2L);
      newContents = new ArrayList<String>(2);
      newContents.add("translated 1");
      newContents.add("translated 2");
      translationReqs.add( new TransUnitUpdateRequest(transUnitId, newContents, ContentState.NeedReview, 0) );

      List<TranslationResult> results = transService.translate(new LocaleId("de"), translationReqs);

      // First result
      TranslationResult result = results.get(0);
      assertThat(result.isTranslationSuccessful(), is(true));
      assertThat(result.getBaseVersionNum(), is(1));
      assertThat(result.getBaseContentState(), is(ContentState.Approved)); // there was a prvious translation
      assertThat(result.getTranslatedTextFlowTarget().getVersionNum(), is(2)); // moved up a version

      // Second result
      result = results.get(1);
      assertThat(result.isTranslationSuccessful(), is(true));
      assertThat(result.getBaseVersionNum(), is(0));
      assertThat(result.getBaseContentState(), is(ContentState.New)); // no previous translation
      assertThat(result.getTranslatedTextFlowTarget().getVersionNum(), is(1)); // first version
   }

   @Test(expectedExceptions = ConcurrentTranslationException.class)
   public void incorrectBaseVersion() throws Exception
   {
      TranslationService transService = seam.autowire(TranslationServiceImpl.class);

      TransUnitId transUnitId = new TransUnitId(2L);
      List<String> newContents = new ArrayList<String>(2);
      newContents.add("translated 1");
      newContents.add("translated 2");
      TransUnitUpdateRequest translateReq = new TransUnitUpdateRequest(transUnitId, newContents, ContentState.Approved, 1);

      // Should not pass as the base version (1) does not match
      List<TransUnitUpdateRequest> translationRequests = Lists.newArrayList(translateReq);
      transService.translate(new LocaleId("de"), translationRequests);
   }
}
