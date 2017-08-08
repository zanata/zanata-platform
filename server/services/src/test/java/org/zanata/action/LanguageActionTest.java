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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.events.JoinedLanguageTeam;
import org.zanata.events.LanguageTeamPermissionChangedEvent;
import org.zanata.events.LeftLanguageTeam;
import org.zanata.i18n.Messages;
import org.zanata.model.*;
import org.zanata.model.type.RequestState;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.LanguageTeamService;
import org.zanata.service.LocaleService;
import org.zanata.service.RequestService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.UrlUtil;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.zanata.test.EntityTestData.setId;

/**
 * @author djansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InSessionScope
@InRequestScope
@RunWith(CdiUnitRunner.class)
public class LanguageActionTest {
    @Produces @Mock
    private LanguageTeamService languageTeamServiceImpl;
    @Produces @Mock
    private LocaleDAO localeDAO;
    @Produces @Mock
    private PersonDAO personDAO;
    @Produces @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LocaleService localeServiceImpl;
    @Produces @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    @Authenticated
    private HAccount authenticatedAccount;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private Messages msgs;
    @Produces @Mock
    private FacesMessages facesMessages;
    @Produces @Mock
    private Event<JoinedLanguageTeam> joinLanguageTeamEvent;
    @Produces @Mock
    private Event<LanguageTeamPermissionChangedEvent> languageTeamPermissionChangedEvent;
    @Produces @Mock
    private Event<LeftLanguageTeam> leaveLanguageTeamEvent;
    @Produces @Mock
    private LocaleMemberDAO localeMemberDAO;
    @Produces @Mock
    private RequestService requestServiceImpl;
    @Produces @Mock
    private ResourceUtils resourceUtils;
    @Produces @Mock
    private UrlUtil urlUtil;
    @Produces @Mock
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private LanguageAction languageAction;

    @Test
    public void adminCanJoinALanguage() {
        languageAction.setLanguage("en-us");
        HPerson person = new HPerson();
        setId(person, 1L);

        when(identity.hasRole("admin")).thenReturn(true);
        when(authenticatedAccount.getPerson()).thenReturn(person);

        languageAction.joinLanguageTeam();
        verify(languageTeamServiceImpl, times(1)).joinOrUpdateRoleInLanguageTeam(
                languageAction.getLanguage(),
                authenticatedAccount.getPerson().getId(),
                true, true, true);
    }

    @Test
    public void userCanJoinIfAutoJoinIsEnabled() {
        languageAction.setLanguage("en-us");
        HPerson person = new HPerson();
        setId(person, 1L);

        when(applicationConfiguration.isAutoAcceptRequests()).thenReturn(true);
        when(identity.hasRole("admin")).thenReturn(false);
        when(authenticatedAccount.getPerson()).thenReturn(person);

        languageAction.joinLanguageTeam();
        verify(languageTeamServiceImpl, times(1)).joinOrUpdateRoleInLanguageTeam(
                languageAction.getLanguage(),
                authenticatedAccount.getPerson().getId(),
                true, false, false);
    }

    @Test
    public void userCannotJoinIfAutoJoinIsDisabled() {
        msgs = new Messages(new Locale("en-us"));
        languageAction.setLanguage("en-us");
        HPerson person = new HPerson();
        setId(person, 1L);

        when(applicationConfiguration.isAutoAcceptRequests()).thenReturn(false);
        when(identity.hasRole("admin")).thenReturn(false);
        when(authenticatedAccount.getPerson()).thenReturn(person);

        languageAction.joinLanguageTeam();
        verify(facesMessages, times(1)).addGlobal(SEVERITY_ERROR, "Access denied");

    }

    @Test
    public void oldRequestsAreRemoved() throws Exception {
        languageAction.setLanguage("en-us");
        HPerson person = new HPerson();
        setId(person, 1L);
        HLocale locale = new HLocale(new LocaleId("en-us"));
        locale.setMembers(Collections.emptySet());
        LanguageRequest languageRequest = new LanguageRequest(
                new Request(), new HLocale(LocaleId.EN), false, false, true);

        when(applicationConfiguration.isAutoAcceptRequests()).thenReturn(true);
        when(identity.hasRole("admin")).thenReturn(false);
        when(authenticatedAccount.getPerson()).thenReturn(person);
        when(requestServiceImpl.getPendingLanguageRequests(authenticatedAccount,
                languageAction.getLocale().getLocaleId())).thenReturn(languageRequest);
        when(localeServiceImpl.getByLocaleId(any(LocaleId.class))).thenReturn(locale);

        languageAction.joinLanguageTeam();
        verify(requestServiceImpl, times(1)).updateLanguageRequest(languageRequest.getId(),
                authenticatedAccount, RequestState.CANCELLED,
                "Outdated language translator request cancelled");
    }
}
