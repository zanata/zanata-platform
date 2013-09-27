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
package org.zanata.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;

@Test(groups = { "jpa-tests" })
public class HTextFlowJPATest extends ZanataDbunitJpaTest {

    private LocaleDAO localeDAO;

    private ProjectIterationDAO projectIterationDAO;

    private HLocale en_US;

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/DocumentsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        afterTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        afterTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        afterTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/DocumentsData.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @BeforeMethod(firstTimeOnly = true)
    public void beforeMethod() {
        localeDAO = new LocaleDAO((Session) em.getDelegate());
        projectIterationDAO =
                new ProjectIterationDAO((Session) em.getDelegate());
        en_US = localeDAO.findByLocaleId(LocaleId.EN_US);
    }

    @Test
    public void textFlowWithSingleContent() {
        HDocument hdoc =
                new HDocument("fullpath", ContentType.TextPlain, en_US);
        hdoc.setProjectIteration(projectIterationDAO.findById(1L, false)); // Taken
                                                                           // from
                                                                           // ProjectsData.dbunit.xml

        HTextFlow tf = new HTextFlow(hdoc, "hello world res id", "hello world");

        hdoc.getTextFlows().add(tf);

        em.persist(hdoc);
        em.flush();

        // reload the doc
        em.refresh(hdoc);

        assertThat("Expected document to contain at least one text flow", hdoc
                .getTextFlows().size(), greaterThan(0));
        assertThat("Expected Text flow to contain single content", hdoc
                .getTextFlows().get(0).getContents().size(), is(1));
        assertThat("Expected deprecated method to still work", hdoc
                .getTextFlows().get(0).getContents().get(0), is("hello world"));
    }

    @Test
    public void textFlowWithPlurals() {
        HDocument hdoc =
                new HDocument("fullpath", ContentType.TextPlain, en_US);
        hdoc.setProjectIteration(projectIterationDAO.findById(1L, false)); // Taken
                                                                           // from
                                                                           // ProjectsData.dbunit.xml

        HTextFlow tf =
                new HTextFlow(hdoc, "hello world res id 1", "hello world");

        List<String> contents = tf.getContents();
        contents.add("hello worlds");
        contents.add("hellos worlds");
        tf.setContents(contents);

        hdoc.getTextFlows().add(tf);

        em.persist(hdoc);
        em.flush();

        // reload the doc
        em.refresh(hdoc);

        assertThat("Expected document to contain at least one text flow", hdoc
                .getTextFlows().size(), greaterThan(0));
        assertThat("Expected Text flow to contain 3 content strings", hdoc
                .getTextFlows().get(0).getContents().size(), is(3));
        assertThat("Expected contents to be preserved", hdoc.getTextFlows()
                .get(0).getContents(), is(Arrays.asList("hello world",
                "hello worlds", "hellos worlds")));
        assertThat("Expected deprecated method to still work", hdoc
                .getTextFlows().get(0).getContents().get(0), is("hello world"));
    }

    @Test
    public void textFlowWithPluralsAndSomeEmptyContents() {
        HDocument hdoc =
                new HDocument("fullpath", ContentType.TextPlain, en_US);
        hdoc.setProjectIteration(projectIterationDAO.findById(1L, false)); // Taken
                                                                           // from
                                                                           // ProjectsData.dbunit.xml

        HTextFlow tf =
                new HTextFlow(hdoc, "hello world res id 1", "hello world");
        tf.setContents("hello world 1", "hello world 2", null, "hello world 4");

        hdoc.getTextFlows().add(tf);

        em.persist(hdoc);
        em.flush();

        // reload the doc
        em.refresh(hdoc);

        assertThat("Expected document to contain at least one text flow", hdoc
                .getTextFlows().size(), greaterThan(0));
        assertThat("Expected Text flow to contain 4 content strings", hdoc
                .getTextFlows().get(0).getContents().size(), is(4));
        assertThat("Expected contents to be preserved", hdoc.getTextFlows()
                .get(0).getContents(), is(Arrays.asList("hello world 1",
                "hello world 2", null, "hello world 4")));
        assertThat("Expected deprecated method to still work", hdoc
                .getTextFlows().get(0).getContents().get(0),
                is("hello world 1"));
    }

}
