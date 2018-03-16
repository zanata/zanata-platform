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
package org.zanata.config;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the Database backed config store.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class DatabaseBackedConfigTest extends ZanataDbunitJpaTest {
    @Inject
    private DatabaseBackedConfig databaseBackedConfig;

    @Produces
    public Session getSession() {
        return super.getSession();
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations
                .add(new DataSetOperation(
                        "org/zanata/test/model/ApplicationConfigurationData.dbunit.xml",
                        DatabaseOperation.CLEAN_INSERT));

        afterTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    @InRequestScope
    public void getHomeContent() {
        assertThat(databaseBackedConfig.getHomeContent())
                .isEqualTo("This is the home content");
    }

    @Test
    @InRequestScope
    public void getAdminEmailAddress() {
        assertThat(databaseBackedConfig.getAdminEmailAddress())
                .isEqualTo("lcroft@redhat.com");
    }

    @Test
    @InRequestScope
    public void getAdminFromAddress() {
        assertThat(databaseBackedConfig.getFromEmailAddress())
                .isEqualTo("aloy@redhat.com");
    }

    @Test
    @InRequestScope
    public void getDomain() {
        assertThat(databaseBackedConfig.getDomain()).isEqualTo("redhat.com");
    }

    @Test
    @InRequestScope
    public void getShouldLogEvents() {
        assertThat(databaseBackedConfig.getShouldLogEvents()).isEqualTo("true");
    }

    @Test
    @InRequestScope
    public void getEmailLogLevel() {
        assertThat(databaseBackedConfig.getEmailLogLevel()).isEqualTo("INFO");
    }

    @Test
    @InRequestScope
    public void getHelpUrl() {
        assertThat(databaseBackedConfig.getHelpUrl()).isEqualTo("http://docs.zanata.org/en/release/");
    }

    @Test
    @InRequestScope
    public void getServerHost() {
        assertThat(databaseBackedConfig.getServerHost()).isEqualTo("http://localhost:8080");
    }

    @Test
    @InRequestScope
    public void getLogEventsDestinationEmailAddress() {
        assertThat(databaseBackedConfig.getLogEventsDestinationEmailAddress())
                .isEqualTo("tihocan@redhat.com");
    }

    @Test
    @InRequestScope
    public void getRegistrationUrl() {
        assertThat(databaseBackedConfig.getRegistrationUrl())
                .isEqualTo("http://zanata.org/register");
    }

    @Test
    @InRequestScope
    public void getPiwikUrl() {
        assertThat(databaseBackedConfig.getPiwikUrl())
                .isEqualTo("http://zanata.org/piwik");
    }

    @Test
    @InRequestScope
    public void getPiwikSiteId() {
        assertThat(databaseBackedConfig.getPiwikSiteId()).isEqualTo("47");
    }

    @Test
    @InRequestScope
    public void getTermsOfUseUrl() {
        assertThat(databaseBackedConfig.getTermsOfUseUrl())
                .isEqualTo("http://zanata.org/terms");
    }

    @Test
    @InRequestScope
    public void getMaxConcurrentRequestsPerApiKey() {
        assertThat(databaseBackedConfig.getMaxConcurrentRequestsPerApiKey())
                .isEqualTo("9");
    }

    @Test
    @InRequestScope
    public void getMaxActiveRequestsPerApiKey() {
        assertThat(databaseBackedConfig.getMaxActiveRequestsPerApiKey()).isEqualTo("8");
    }

    @Test
    @InRequestScope
    public void getMaxFilesPerUpload() {
        assertThat(databaseBackedConfig.getMaxFilesPerUpload()).isEqualTo("7");
    }

    @Test
    @InRequestScope
    public void isDisplayUserEmail() {
        assertThat(databaseBackedConfig.isDisplayUserEmail()).isTrue();
    }

    @Test
    @InRequestScope
    public void getPermittedEmailDomains() {
        assertThat(databaseBackedConfig.getPermittedEmailDomains()).isEqualTo("horizon.com");
    }

    @Test
    @InRequestScope
    public void getNonExistentValue() throws Exception {
        // Prematurely clean out data, assert missing value is null
        cleanDataAfterTest();
        assertThat(databaseBackedConfig.getAdminEmailAddress()).isNull();
    }

    @Test
    @InRequestScope
    public void autoAcceptTranslatorIsFalseIfNull() {
        assertThat(databaseBackedConfig.isAutoAcceptTranslators()).isFalse();
    }

    @Test
    @InRequestScope
    public void getGravatarRating() {
        assertThat(databaseBackedConfig.getMaxGravatarRating()).isEqualTo("R");
    }

    @Test
    @InRequestScope
    public void gravatarRatingDefaultsToGeneral() {
        // Prematurely clean out data, assert missing value is null
        cleanDataAfterTest();
        assertThat(databaseBackedConfig.getMaxGravatarRating()).isEqualTo("G");
    }
}
