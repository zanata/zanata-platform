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
package org.zanata.service.impl;

import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.zanata.ApplicationConfiguration;
import org.zanata.action.VersionGroupJoinAction;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.email.ContactLanguageCoordinatorEmailStrategy;
import org.zanata.email.EmailBuilder;
import org.zanata.email.EmailStrategy;
import org.zanata.i18n.Messages;
import org.zanata.seam.security.IdentityManager;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InRequestScope
@RunWith(CdiUnitRunner.class)
public class EmailServiceImplTest {

    @Mock
    @Produces
    private EmailBuilder emailBuilder;
    @Mock
    @Produces
    private IdentityManager identityManager;
    @Mock
    @Produces
    private PersonDAO personDAO;
    @Mock
    @Produces
    private ApplicationConfiguration applicationConfiguration;
    @Mock
    @Produces
    private VersionGroupJoinAction versionGroupJoinAction;
    @Mock
    @Produces
    private LocaleDAO localeDAO;

    @Produces
    private Messages msgs;

    @Inject
    EmailServiceImpl emailService;

    @Test
    public void emailFailsOnInvalidLocale() {
        msgs = new Messages(new Locale("en"));
        String a = "test";
        EmailStrategy emailStrategy = new ContactLanguageCoordinatorEmailStrategy(
                a, a, a, a, a, a, a, a);
        LocaleId localeId = new LocaleId("en");
        Mockito.when(localeDAO.findByLocaleId(localeId)).thenReturn(null);
        assertThat(emailService.sendToLanguageCoordinators(localeId, emailStrategy))
                .isEqualTo("Error sending email, locale is invalid");
    }
}
