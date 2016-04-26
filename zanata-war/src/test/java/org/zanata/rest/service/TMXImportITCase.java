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

package org.zanata.rest.service;

import java.io.ByteArrayInputStream;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ArquillianTest;
import org.zanata.model.tm.TransMemory;
import org.zanata.provider.DBUnitProvider;
import org.zanata.security.SimplePrincipal;
import org.zanata.security.ZanataIdentity;
import com.google.common.base.Charsets;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TMXImportITCase extends ArquillianTest {
    private static int totalTranslationUnit = 201;

    @Inject
    private TranslationMemoryResourceService tmxResource;

    @Inject
    private ZanataIdentity identity;

    @Inject
    private EntityManager entityManager;

    private static String generateTestTMXContent() {
        String bodyStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                    "<!DOCTYPE tmx SYSTEM \"http://www.lisa.org/tmx/tmx14.dtd\">\n" +
                "<tmx version=\"1.4\">\n" +
                "  <header creationtool=\"Zanata TranslationsTMXExportStrategy\"\n" +
                "    creationtoolversion=\"unknown\" segtype=\"block\"\n" +
                "    o-tmf=\"unknown\" adminlang=\"en\" srclang=\"*all*\"\n" +
                "    datatype=\"unknown\"/>\n" +
                "  <body>";
        String bodyEnd = "  </body></tmx>";
        String transUnits = "";
        // batch size is 100 ATM in org.zanata.tmx.TMXParser
        for (int i = 0; i < totalTranslationUnit; i++) {
            transUnits += makeSingleTU();
        }
        return bodyStart + transUnits + bodyEnd;
    }

    private static String makeSingleTU() {
        return String
                .format("<tu srclang=\"en-US\" tuid=\"about-fedora:master:About_Fedora:%s\">\n" +
                                "      <tuv xml:lang=\"en-US\">\n" +
                                "        <seg>%s</seg>\n" +
                                "      </tuv>\n" +
                                "      <tuv xml:lang=\"pl\">\n" +
                                "        <seg>%s</seg>\n" +
                                "      </tuv>\n" +
                                "    </tu>", randomAlphanumeric(32),
                        randomAlphabetic(10), randomAlphabetic(10));
    }

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/TMXTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Before
    public void setUp() {
        identity.setApiKey("b6d7044e9ee3b2447c28fb7c50d86d98");
        identity.acceptExternallyAuthenticatedPrincipal(
                new SimplePrincipal("admin"));
        identity.addRole("admin");
    }

    @Test
    public void importTMX() throws Exception {
        String tmxContent = generateTestTMXContent();
        tmxResource.updateTranslationMemory("tm-test",
                new ByteArrayInputStream(tmxContent.getBytes(Charsets.UTF_8)));

        TransMemory tm = entityManager
                .createQuery(
                        "from TransMemory as tm inner join fetch tm.translationUnits",
                        TransMemory.class).getSingleResult();
        Assertions.assertThat(tm.getSlug()).isEqualTo("tm-test");
        Assertions.assertThat(tm.getTranslationUnits())
                .hasSize(totalTranslationUnit);
    }
}
