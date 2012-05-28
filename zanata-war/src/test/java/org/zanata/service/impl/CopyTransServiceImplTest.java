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
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.CopyTransService;
import org.zanata.service.LocaleService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CopyTransServiceImplTest extends ZanataDbunitJpaTest
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
   public void copyTrans()
   {
      CopyTransService copyTransService = seam.autowire(CopyTransServiceImpl.class);
      DocumentDAO documentDAO = seam.autowire(DocumentDAO.class);
      ProjectIterationDAO iterationDAO = seam.autowire(ProjectIterationDAO.class);
      LocaleService localeService = seam.autowire(LocaleServiceImpl.class);

      HProjectIteration iter = iterationDAO.getBySlug("sample-project", "1.0");
      HDocument doc = documentDAO.getByDocIdAndIteration(iter, "/my/path/document.txt");

      // Create a new iteration for the project
      HProjectIteration newIter = new HProjectIteration();
      newIter.setSlug("2.0");
      newIter.setProject(iter.getProject());
      // add the same documents to the iteration
      for( HDocument d : iter.getDocuments().values() )
      {
         HDocument newDoc = cloneDocument(d);
         newDoc.setProjectIteration(newIter);
         newIter.getDocuments().put(newDoc.getDocId(), newDoc);
      }
      newIter = iterationDAO.makePersistent(newIter);

      // Get one of the newly created docs (with the same docId as the old one)
      HDocument newDoc = documentDAO.getByDocIdAndIteration(newIter, doc.getDocId());

      // find equivalent translations
      copyTransService.copyTransForDocument( newDoc );

      // Reload the new document and make sure translations were copied
      newDoc = documentDAO.getById( newDoc.getId() );

      // There should be a translation for 'as' and one for 'de' for textFlow with resId 'tf1'
      HLocale asLang = localeService.getByLocaleId("as");
      HLocale deLang = localeService.getByLocaleId("de");
      HTextFlow tf = newDoc.getTextFlows().get(0);

      assertThat(tf.getTargets().get(asLang).getContents(), notNullValue());
      assertThat(tf.getTargets().get(deLang).getContents(), notNullValue());

      // make sure the translation is the same as the translation on the original document
      HTextFlow originalTf = doc.getTextFlows().get(0);
      assertThat(tf.getTargets().get(asLang).getContents(), equalTo(originalTf.getTargets().get(asLang).getContents()));
      assertThat(tf.getTargets().get(deLang).getContents(), equalTo(originalTf.getTargets().get(deLang).getContents()));
   }

   @Test
   public void copyTransWithDuplicates()
   {
      CopyTransService copyTransService = seam.autowire(CopyTransServiceImpl.class);
      DocumentDAO documentDAO = seam.autowire(DocumentDAO.class);
      ProjectIterationDAO iterationDAO = seam.autowire(ProjectIterationDAO.class);
      LocaleService localeService = seam.autowire(LocaleServiceImpl.class);

      HLocale asLang = localeService.getByLocaleId("as");
      HLocale deLang = localeService.getByLocaleId("de");

      HProjectIteration iter = iterationDAO.getBySlug("sample-project", "1.0");
      HDocument doc = documentDAO.getByDocIdAndIteration(iter, "/my/path/document.txt");

      // Add a duplicate text flow (same content, different resId)
      HTextFlow newTextFlow = new HTextFlow();
      newTextFlow.setResId("newresid");
      newTextFlow.setDocument(doc);
      newTextFlow.setPlural( false );
      newTextFlow.setContents("hello world");
      newTextFlow.setObsolete(false);
      // Add a translation (as)
      HTextFlowTarget newTextFlowTrans = new HTextFlowTarget(newTextFlow, asLang);
      newTextFlowTrans.setContents("yello mondo");
      newTextFlowTrans.setState(ContentState.Approved);
      newTextFlow.getTargets().put( newTextFlowTrans.getLocale(), newTextFlowTrans );
      // Add a translation (de)
      HTextFlowTarget newTextFlowTrans2 = new HTextFlowTarget(newTextFlow, deLang);
      newTextFlowTrans2.setContents("hallo welt");
      newTextFlowTrans2.setState(ContentState.Approved);
      newTextFlow.getTargets().put( newTextFlowTrans2.getLocale(), newTextFlowTrans2 );

      doc.getTextFlows().add(newTextFlow);
      documentDAO.makePersistent(doc);
      documentDAO.flush();

      // Create a new iteration for the project
      HProjectIteration newIter = new HProjectIteration();
      newIter.setSlug("2.0");
      newIter.setProject(iter.getProject());
      // add the same documents to the iteration
      for( HDocument d : iter.getDocuments().values() )
      {
         HDocument newDoc = cloneDocument(d);
         newDoc.setProjectIteration(newIter);
         newIter.getDocuments().put(newDoc.getDocId(), newDoc);
      }
      newIter = iterationDAO.makePersistent(newIter);

      // Get one of the newly created docs (with the same docId as the old one)
      HDocument newDoc = documentDAO.getByDocIdAndIteration(newIter, doc.getDocId());

      // find equivalent translations
      copyTransService.copyTransForDocument( newDoc );

      // Reload the new document and make sure translations were copied
      newDoc = documentDAO.getById( newDoc.getId() );

      // There should be a translation for 'as' and one for 'de' for textFlow with resId 'tf1'
      // as well as translations for textFlow with resId 'newresid'
      HTextFlow tf = newDoc.getTextFlows().get(0);

      assertThat(tf.getTargets().get(asLang).getContents(), notNullValue());
      assertThat(tf.getTargets().get(deLang).getContents(), notNullValue());

      // make sure the translation is the same as the translation on the original document
      HTextFlow originalTf = doc.getTextFlows().get(0);
      assertThat(tf.getTargets().get(asLang).getContents(), equalTo(originalTf.getTargets().get(asLang).getContents()));
      assertThat(tf.getTargets().get(deLang).getContents(), equalTo(originalTf.getTargets().get(deLang).getContents()));

      // Same checks for the other text flow with same content
      tf = newDoc .getTextFlows().get(1);
      assertThat(tf.getTargets().get(asLang).getContents(), notNullValue());
      assertThat(tf.getTargets().get(deLang).getContents(), notNullValue());

      assertThat(tf.getTargets().get(asLang).getContents(), equalTo(newTextFlow.getTargets().get(asLang).getContents()));
      assertThat(tf.getTargets().get(deLang).getContents(), equalTo(newTextFlow.getTargets().get(deLang).getContents()));
   }

   private static HDocument cloneDocument(HDocument document)
   {
      HDocument clone = new HDocument(document.getDocId(), document.getContentType(), document.getLocale() );

      for( HTextFlow tf : document.getTextFlows() )
      {
         HTextFlow newTf = new HTextFlow();
         newTf.setResId( tf.getResId() );
         newTf.setDocument( tf.getDocument() );
         //newTf.setComment( tf.getComment() );
         newTf.setPlural(tf.isPlural());
         newTf.setContents(tf.getContents());
         //newTf.setPotEntryData( tf.getPotEntryData() );
         newTf.setObsolete(false);

         clone.getTextFlows().add(newTf);
      }
      return clone;
   }
}
