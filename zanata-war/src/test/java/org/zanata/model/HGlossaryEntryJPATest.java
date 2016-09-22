/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import java.util.List;
import java.util.Map;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.util.GlossaryUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class HGlossaryEntryJPATest extends ZanataDbunitJpaTest {
    private GlossaryDAO glossaryDAO;

    @Before
    public void beforeMethod() {
        glossaryDAO = new GlossaryDAO((Session) em.getDelegate());
    }

    @Test
    public void testHashMap() {
        List<HGlossaryEntry> entryList = glossaryDAO.getEntriesByLocale(
                LocaleId.EN_US, 0, 200, null, null, GlossaryUtil.GLOBAL_QUALIFIED_NAME);

        for (HGlossaryEntry hGlossaryEntry : entryList) {
            for (Map.Entry<HLocale, HGlossaryTerm> entry : hGlossaryEntry
                    .getGlossaryTerms().entrySet()) {
                Assert.assertTrue(hGlossaryEntry.getGlossaryTerms()
                        .containsKey(entry.getKey()));
                Assert.assertNotNull(hGlossaryEntry.getGlossaryTerms().get(
                        entry.getKey()));
            }
        }

    }

    @Test
    public void testTermsSize() {
        List<HGlossaryEntry> entryList = glossaryDAO.getEntriesByLocale(
            LocaleId.EN_US, 0, 200, null, null, GlossaryUtil.GLOBAL_QUALIFIED_NAME);
        assertThat(entryList.get(0).getGlossaryTerms().size(), is(3));
    }

    @Test
    public void testDeleteGlossaries() {
        glossaryDAO.deleteAllEntries(GlossaryUtil.GLOBAL_QUALIFIED_NAME);
        glossaryDAO.flush();

        List<HGlossaryEntry> entryList = glossaryDAO.getEntriesByLocale(
            LocaleId.EN_US, 0, 200, null, null, GlossaryUtil.GLOBAL_QUALIFIED_NAME);

        assertThat(entryList.size(), is(0));
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/GlossaryData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

    }
}
