/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.zanata.config.DatabaseBackedConfig;
import org.zanata.config.JaasConfig;
import org.zanata.config.SystemPropertyConfigStore;
import org.zanata.events.ConfigurationChanged;
import org.zanata.i18n.Messages;
import org.zanata.log4j.ZanataSMTPAppender;
import org.zanata.model.HApplicationConfiguration;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.DefaultLocale;
import org.zanata.util.Synchronized;

import com.google.common.collect.Lists;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@ApplicationScoped
@Synchronized(timeout = ServerConstants.DEFAULT_TIMEOUT)
@RunWith(CdiUnitRunner.class)
public class ApplicationConfigurationTest implements Serializable {
    private static final long serialVersionUID = 1L;
    @Produces
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseBackedConfig databaseBackedConfig;
    @Produces
    @Mock
    private JaasConfig jaasConfig;
    @Produces
    @DefaultLocale
    @Mock
    private Messages msgs;
    @Produces
    @Mock
    private SystemPropertyConfigStore sysPropConfigStore;
    @Produces
    @DeltaSpike
    @Mock
    private HttpSession session;

    @Inject
    ApplicationConfiguration applicationConfiguration;

    @Test
    public void testGravatarAccessor() {
        when(databaseBackedConfig.getMaxGravatarRating()).thenReturn("test");
        assertThat(applicationConfiguration.getGravatarRating()).isEqualTo("test");
    }

    @Test
    public void testTMFuzzyBand() {
        when(databaseBackedConfig.getTMFuzzyBands()).thenReturn("80 90");
        assertThat(applicationConfiguration.getTMFuzzyBands()).isEqualTo("80 90");
    }

    @Test
    public void canApplyLoggingConfiguration() {
        when(databaseBackedConfig.getFromEmailAddress())
                .thenReturn("from@example.com");
        when(databaseBackedConfig.getEmailLogLevel()).thenReturn("INFO");
        when(databaseBackedConfig.getLogEventsDestinationEmailAddress())
                .thenReturn("to@example.com");
        when(databaseBackedConfig.getShouldLogEvents()).thenReturn("true");

        applicationConfiguration.applyLoggingConfiguration();

        ZanataSMTPAppender smtpAppender =
                applicationConfiguration.getSMTPAppender();
        assertThat(smtpAppender.getName())
                .isEqualTo(ApplicationConfiguration.EMAIL_APPENDER_NAME);
        assertThat(smtpAppender.getFrom()).isEqualTo("from@example.com");
        assertThat(smtpAppender.getTo()).isEqualTo("to@example.com");

        org.apache.log4j.Logger rootLogger =
                org.apache.log4j.Logger.getRootLogger();
        assertThat(rootLogger
                .getAppender(ApplicationConfiguration.EMAIL_APPENDER_NAME))
                        .isSameAs(smtpAppender);
    }

    @Test
    public void willUpdateLoggingConfigOnRelevantConfigChange() {
        when(databaseBackedConfig.getFromEmailAddress())
                .thenReturn("from@example.com");
        when(databaseBackedConfig.getEmailLogLevel()).thenReturn("INFO");
        when(databaseBackedConfig.getLogEventsDestinationEmailAddress())
                .thenReturn("to@example.com");
        when(databaseBackedConfig.getShouldLogEvents()).thenReturn("true");

        applicationConfiguration.applyLoggingConfiguration();

        List<String> relevantConfigKeys = Lists.newArrayList(
                HApplicationConfiguration.KEY_EMAIL_FROM_ADDRESS,
                HApplicationConfiguration.KEY_EMAIL_LOG_EVENTS,
                HApplicationConfiguration.KEY_EMAIL_LOG_LEVEL,
                HApplicationConfiguration.KEY_LOG_DESTINATION_EMAIL);

        for (int i = 0; i < relevantConfigKeys.size(); i++) {
            when(databaseBackedConfig.getFromEmailAddress())
                    .thenReturn(i + "-from@example.com");
            when(databaseBackedConfig.getEmailLogLevel()).thenReturn("INFO");
            when(databaseBackedConfig.getLogEventsDestinationEmailAddress())
                    .thenReturn(i + "-to@example.com");
            boolean shouldLogToEmail = i % 2 == 0;
            when(databaseBackedConfig.getShouldLogEvents())
                    .thenReturn(Boolean.toString(shouldLogToEmail));
            String configKey = relevantConfigKeys.get(i);
            applicationConfiguration
                    .configChanged(new ConfigurationChanged(configKey));

            ZanataSMTPAppender smtpAppender =
                    applicationConfiguration.getSMTPAppender();

            assertThat(smtpAppender.getFrom())
                    .isEqualTo(i + "-from@example.com");
            assertThat(smtpAppender.getTo()).isEqualTo(i + "-to@example.com");

            org.apache.log4j.Logger rootLogger =
                    org.apache.log4j.Logger.getRootLogger();
            if (shouldLogToEmail) {
                assertThat(rootLogger.getAppender(
                        ApplicationConfiguration.EMAIL_APPENDER_NAME))
                                .isSameAs(smtpAppender);
            } else {
                assertThat(rootLogger.getAppender(
                        ApplicationConfiguration.EMAIL_APPENDER_NAME)).isNull();
            }
        }
    }


}
