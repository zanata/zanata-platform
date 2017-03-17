/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.action;

import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.InSessionScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;

import org.zanata.ApplicationConfiguration;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.exception.RequestExistsException;
import org.zanata.i18n.Messages;
import org.zanata.model.*;
import org.zanata.model.type.RequestState;
import org.zanata.model.type.RequestType;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.EmailService;
import org.zanata.service.LanguageTeamService;
import org.zanata.service.LocaleService;
import org.zanata.service.RequestService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.faces.FacesMessages;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author djansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InSessionScope
@InRequestScope
@RunWith(CdiUnitRunner.class)
public class LanguageJoinActionTest {

    @Produces
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LocaleService localeServiceImpl;

    @Mock
    @Produces
    private EmailService emailServiceImpl;

    @Mock
    @Produces
    private FacesMessages facesMessages;

    @Mock
    @Produces
    private Messages msgs;

    @Mock
    @Produces
    private RequestService requestServiceImpl;

    @Mock
    @Produces
    private ZanataIdentity identity;

    @Produces
    @Mock
    private LanguageTeamService languageTeamServiceImpl;

    @Mock
    @Produces
    private ApplicationConfiguration applicationConfiguration;

    @Mock
    @Produces
    private LocaleMemberDAO localeMemberDAO;

    @Produces
    @Authenticated
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HAccount authenticatedAccount;

    @Inject
    LanguageJoinAction languageJoinAction;

    @Before
    public void before() {
        when(authenticatedAccount.getId()).thenReturn(1234567890L);
        languageJoinAction.setLanguage("en");
        when(authenticatedAccount.getUsername()).thenReturn("aloy");
    }

    @Test
    public void testLocaleMember() {
        HLocaleMember hLocaleMember = new HLocaleMember();
        hLocaleMember.setCoordinator(true);
        hLocaleMember.setReviewer(false);
        hLocaleMember.setTranslator(true);
        languageJoinAction.setLanguage("en");
        when(localeMemberDAO.findByPersonAndLocale(anyLong(), any(LocaleId.class)))
                .thenReturn(hLocaleMember);

        assertThat(languageJoinAction.isCoordinator()).isTrue();
        assertThat(languageJoinAction.isReviewer()).isFalse();
        assertThat(languageJoinAction.isTranslator()).isTrue();
    }

    @Test
    public void testTranslatorRequestAutoGrantedIfSet() {
        HLocaleMember hLocaleMember = new HLocaleMember();
        hLocaleMember.setCoordinator(true);
        hLocaleMember.setReviewer(false);
        hLocaleMember.setTranslator(true);
        languageJoinAction.setLanguage("en");

        when(localeMemberDAO.findByPersonAndLocale(anyLong(), any(LocaleId.class)))
                .thenReturn(hLocaleMember);
        when(applicationConfiguration.isAutoAcceptRequests()).thenReturn(true);

        languageJoinAction.processRequest(true, false, false);

        verify(languageTeamServiceImpl, times(1))
                .joinOrUpdateRoleInLanguageTeam("en", 1234567890L,
                        true, false, false);
    }

    @Test
    public void testTranslatorRequestNotAutoGrantedIfNotSet() {
        String language = "en";
        HLocale hLocale = new HLocale(new LocaleId(language));
        hLocale.setMembers(Collections.emptySet());
        languageJoinAction.setLanguage(language);

        when(applicationConfiguration.isAutoAcceptRequests()).thenReturn(false);
        when(localeServiceImpl.getByLocaleId(new LocaleId("en")))
                .thenReturn(hLocale);

        languageJoinAction.processRequest(true, false, false);

        verify(requestServiceImpl, times(1))
                .createLanguageRequest(authenticatedAccount, hLocale,
                        false, false, true);
    }

    @Test
    public void testReviewerOrCoordinatorRequestNotAutoGranted() {
        String language = "en";
        HLocale hLocale = new HLocale(new LocaleId(language));
        hLocale.setMembers(Collections.emptySet());
        languageJoinAction.setLanguage(language);

        when(applicationConfiguration.isAutoAcceptRequests()).thenReturn(true);
        when(localeServiceImpl.getByLocaleId(new LocaleId("en")))
                .thenReturn(hLocale);

        languageJoinAction.processRequest(true, true, false);
        languageJoinAction.processRequest(true, false, true);

        verify(requestServiceImpl, times(1))
                .createLanguageRequest(authenticatedAccount, hLocale,
                        false, true, true);
        verify(requestServiceImpl, times(1))
                .createLanguageRequest(authenticatedAccount, hLocale,
                        true, false, true);
    }

    @Test
    public void cannotCreateARequestIfAlreadyExists() {
        String language = "en";
        HLocale hLocale = new HLocale(new LocaleId(language));
        hLocale.setMembers(Collections.emptySet());
        languageJoinAction.setLanguage(language);

        when(msgs.format("jsf.language.request.exists", "aloy", "English"))
                .thenReturn("Correct message!");

        when(applicationConfiguration.isAutoAcceptRequests()).thenReturn(false);
        when(localeServiceImpl.getByLocaleId(new LocaleId("en")))
                .thenReturn(hLocale);
        when(requestServiceImpl.createLanguageRequest(authenticatedAccount, hLocale,
                        false, false, true))
                .thenThrow(RequestExistsException.class);

        languageJoinAction.processRequest(true, false, false);

        verify(msgs, times(1)).format("jsf.language.request.exists", "aloy", "English");
        verify(facesMessages, times(1)).addGlobal("Correct message!");
    }

    @Test
    public void testAcceptAction() {
        HLocaleMember hLocaleMember = new HLocaleMember();
        hLocaleMember.setCoordinator(true);
        hLocaleMember.setReviewer(false);
        hLocaleMember.setTranslator(true);
        languageJoinAction.setLanguage("en");
        LanguageRequest languageRequest = new LanguageRequest();
        languageRequest.setRequest(new Request(RequestType.LOCALE,
                authenticatedAccount, "test", new Date()));
        when(requestServiceImpl.getLanguageRequest(987654321L)).thenReturn(languageRequest);
        when(localeMemberDAO.findByPersonAndLocale(anyLong(), any(LocaleId.class)))
                .thenReturn(hLocaleMember);
        languageJoinAction.acceptRequest(987654321L);

        verify(languageTeamServiceImpl, times(1)).joinOrUpdateRoleInLanguageTeam("en",
                0L, true, false, true);
        verify(requestServiceImpl, times(1)).updateLanguageRequest(
                987654321L, authenticatedAccount, RequestState.ACCEPTED, "");
    }

    @Test
    public void testDeclineAction() {
        String declineMessage = "Not today, disco lady";
        languageJoinAction.setDeclineMessage(declineMessage);
        languageJoinAction.declineRequest(987654321L);

        verify(requestServiceImpl, times(1)).updateLanguageRequest(
                987654321L, authenticatedAccount, RequestState.REJECTED,
                declineMessage);
    }
}
