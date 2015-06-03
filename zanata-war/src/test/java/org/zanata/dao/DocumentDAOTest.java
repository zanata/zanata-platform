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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import javax.annotation.Nullable;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

import com.google.common.base.Function;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DocumentDAOTest extends ZanataDbunitJpaTest {

    // Static variables to work with the same document for all tests
    private static final String PROJECT_SLUG = "sample-project";
    private static final String ITERATION_SLUG = "1.0";
    private static final String DOC_ID = "my/path/document.txt";

    private HLocale as;
    private HLocale de;

    private DocumentDAO documentDAO;

    private LocaleDAO localeDAO;

    @Before
    public void setup() {
        documentDAO = new DocumentDAO(getSession());
        localeDAO = new LocaleDAO(getSession());
        as = localeDAO.findByLocaleId(new LocaleId("as"));
        de = localeDAO.findByLocaleId(new LocaleId("de"));
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
    public void repeatableDocumentStateHash() throws Exception {
        String docHash =
                documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG,
                        ITERATION_SLUG, DOC_ID, as);

        // Make sure it's repeatable
        for (int i = 0; i < 5; i++) {
            String unchangedHash =
                    documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG,
                            ITERATION_SLUG, DOC_ID, as);
            assertThat(
                    "Translated Document state hash function not repeatable",
                    unchangedHash, equalTo(docHash));
        }
    }

    private void testHashChange(Function<HDocument, Void> mutator,
            boolean expectHashChange) throws Exception {
        String docHash =
                documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG,
                        ITERATION_SLUG, DOC_ID, as);
        // Translate something in the document
        HDocument doc =
                documentDAO.getByProjectIterationAndDocId(PROJECT_SLUG,
                        ITERATION_SLUG, DOC_ID);
        mutator.apply(doc);

        // force a flush on the DB
        getSession().flush();

        // Hash must change
        String changedDocHash =
                documentDAO.getTranslatedDocumentStateHash(PROJECT_SLUG,
                        ITERATION_SLUG, DOC_ID, as);
        if (expectHashChange) {
            assertThat(
                    "Translated document hash must change when something is changed",
                    changedDocHash, not(equalTo(docHash)));
        } else {
            assertThat(
                    "Translated document hash must not change when nothing is changed",
                    changedDocHash, equalTo(docHash));
        }
    }

    @Test()
    public void noChangeMeansNoHashChange() throws Exception {
        testHashChange(new Function<HDocument, Void>() {
            @Override
            @Nullable
            public Void apply(@Nullable HDocument doc) {
                // change nothing
                return null;
            }
        }, false);
    }

    @Test
    public void simpleDocumentUpdateChangesHash() throws Exception {
        testHashChange(new Function<HDocument, Void>() {
            @Override
            @Nullable
            public Void apply(@Nullable HDocument doc) {
                doc.setName("newdocname.txt");
                return null;
            }
        }, true);
    }

    @Test
    public void translationChangesHash() throws Exception {
        testHashChange(new Function<HDocument, Void>() {
            @Override
            @Nullable
            public Void apply(@Nullable HDocument doc) {
                HTextFlow tf = doc.getTextFlows().get(0);
                HTextFlowTarget tft = tf.getTargets().get(as.getId());
                tft.setContent0("new Translation for as");
                return null;
            }
        }, true);
    }

    @Test
    public void otherLangDoesNotChangeHash() throws Exception {
        testHashChange(new Function<HDocument, Void>() {
            @Override
            @Nullable
            public Void apply(@Nullable HDocument doc) {
                HTextFlow tf = doc.getTextFlows().get(0);
                HTextFlowTarget tft = tf.getTargets().get(de.getId());
                tft.setContent0("new Translation for de");
                return null;
            }
        }, false);
    }

    @Test
    public void tftNewCommentChangesHash() throws Exception {
        testHashChange(new Function<HDocument, Void>() {
            @Override
            @Nullable
            public Void apply(@Nullable HDocument doc) {
                HTextFlow tf = doc.getTextFlows().get(0);
                HTextFlowTarget tft = tf.getTargets().get(as.getId());
                tft.setComment(new HSimpleComment("This is a new comment"));
                return null;
            }
        }, true);
    }

    // TODO set up the dbunit data with a pre-existing comment so that we can use this test
//    @Test
//    public void tftCommentChangesHash() throws Exception {
//        testHashChange(new Function<HDocument, Void>() {
//            @Override
//            @Nullable
//            public Void apply(@Nullable HDocument doc) {
//                HTextFlow tf = doc.getTextFlows().get(0);
//                HTextFlowTarget tft = tf.getTargets().get(as.getId());
//                // this requires changes to our dbunit config:
//                tft.getComment().setComment("This is a modified comment");
//                return null;
//            }
//        }, true);
//    }
}
