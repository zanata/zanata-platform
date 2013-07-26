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
package org.zanata.dao;

import org.dbunit.operation.DatabaseOperation;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Test(groups = { "jpa-tests" })
public class DocumentDAOTest extends ZanataDbunitJpaTest
{

   // Static variables to work with the same document for all tests
   private static final String PROJECT_SLUG =      "sample-project";
   private static final String ITERATION_SLUG =    "1.0";
   private static final String DOC_ID =            "my/path/document.txt";

   private HLocale as;
   private HLocale bn;

   private DocumentDAO documentDAO;

   private LocaleDAO localeDAO;

   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      documentDAO = new DocumentDAO(getSession());
      localeDAO = new LocaleDAO(getSession());
      as = localeDAO.findByLocaleId(new LocaleId("as"));
      bn = localeDAO.findByLocaleId(new LocaleId("bn"));
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Test
   public void repeatableDocumentStateHash() throws Exception
   {
      String docHash =
         documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG, ITERATION_SLUG, DOC_ID, as);

      // Make sure it's repeatable
      for( int i=0; i<5; i++ )
      {
         String unchangedHash =
               documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG, ITERATION_SLUG, DOC_ID, as);
         assertThat("Translated Document state hash function not repeatable", unchangedHash, equalTo(docHash));
      }
   }

   @Test
   public void simpleDocumentUpdateChangesHash() throws Exception
   {
      String docHash =
            documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG, ITERATION_SLUG, DOC_ID, as);

      // Change the document's name
      HDocument doc = documentDAO.getByProjectIterationAndDocId(PROJECT_SLUG, ITERATION_SLUG, DOC_ID);
      doc.setName("newdocname.txt");

      // force a flush on the DB
      getSession().flush();

      // Hash must change
      String changedDocHash =
            documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG, ITERATION_SLUG, DOC_ID, as);

      assertThat("Translated document hash must change when document is changed", changedDocHash, not(equalTo( docHash )));
   }

   @Test
   public void translationChangesHash() throws Exception
   {
      String docHash =
            documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG, ITERATION_SLUG, DOC_ID, as);
      String bnDocHash =
            documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG, ITERATION_SLUG, DOC_ID, bn);

      // Translate something in the document
      HDocument doc = documentDAO.getByProjectIterationAndDocId(PROJECT_SLUG, ITERATION_SLUG, DOC_ID);
      HTextFlow tf = doc.getTextFlows().get(0);
      HTextFlowTarget tft = tf.getTargets().get( new Long(1) ); // 'as' target
      tft.setContent0("new Translation for as");

      // force a flush on the DB
      getSession().flush();

      // Hash must change
      String changedDocHash =
            documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG, ITERATION_SLUG, DOC_ID, bn);

      assertThat("Translated document hash must change when translation is changed", changedDocHash, not(equalTo( docHash )));

      // Make sure other language's state hash did not change
      String changedBnDocHash =
            documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG, ITERATION_SLUG, DOC_ID, bn);

      assertThat("Translated document hash must not change when translation for other language is changed", changedBnDocHash, equalTo(bnDocHash));
   }

   @Test
   public void tftCommentChangesHash() throws Exception
   {
      String docHash = documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG, ITERATION_SLUG, DOC_ID, as);

      // Translate something in the document
      HDocument doc = documentDAO.getByProjectIterationAndDocId(PROJECT_SLUG, ITERATION_SLUG, DOC_ID);
      HTextFlow tf = doc.getTextFlows().get(0);
      HTextFlowTarget tft = tf.getTargets().get( new Long(1) ); // 'as' target
      tft.setComment( new HSimpleComment("This is a new comment"));

      // force a flush on the DB
      getSession().flush();

      // Hash must change
      String changedDocHash =
            documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG, ITERATION_SLUG, DOC_ID, bn);

      assertThat("Translated document hash must change when translation is changed", changedDocHash, not(equalTo( docHash )));
   }
}
