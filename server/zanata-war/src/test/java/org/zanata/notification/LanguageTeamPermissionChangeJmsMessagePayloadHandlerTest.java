package org.zanata.notification;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.mail.internet.InternetAddress;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.LocaleId;
import org.zanata.email.EmailBuilder;
import org.zanata.email.LanguageTeamPermissionChangeEmailStrategy;
import org.zanata.events.LanguageTeamPermissionChangedEvent;
import org.zanata.i18n.Messages;

public class LanguageTeamPermissionChangeJmsMessagePayloadHandlerTest {
    private LanguageTeamPermissionChangeJmsMessagePayloadHandler handler;
    @Mock
    private EmailBuilder emailBuilder;

    @Mock
    private LanguageTeamPermissionChangedEvent permissionChangeEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        handler =
                new LanguageTeamPermissionChangeJmsMessagePayloadHandler(
                        emailBuilder, new Messages(Locale.ENGLISH), "http://localhost");

    }

    @Test
    public void willNotHandleWhenEventIsIrrelevant() {
        handler.handle("not a language team permission change event");

        Mockito.verifyZeroInteractions(emailBuilder);
    }

    @Test
    public void willNotHandleIfTeamPermissionHasNotChanged() {
        when(permissionChangeEvent.hasPermissionsChanged()).thenReturn(false);

        handler.handle(permissionChangeEvent);

        Mockito.verifyZeroInteractions(emailBuilder);
    }

    @Test
    public void willSendEmailToAffectedPerson() {
        when(permissionChangeEvent.hasPermissionsChanged()).thenReturn(true);
        when(permissionChangeEvent.getLanguage()).thenReturn(LocaleId.DE);
        when(permissionChangeEvent.getEmail()).thenReturn("john@a.c");
        when(permissionChangeEvent.getName()).thenReturn("John Smith");

        handler.handle(permissionChangeEvent);

        ArgumentCaptor<LanguageTeamPermissionChangeEmailStrategy> strategyArgumentCaptor =
                ArgumentCaptor
                        .forClass(
                        LanguageTeamPermissionChangeEmailStrategy.class);
        ArgumentCaptor<List> receivedReasonCaptor =
                ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<InternetAddress> toAddressCaptor =
                ArgumentCaptor.forClass(InternetAddress.class);

        verify(emailBuilder).sendMessage(strategyArgumentCaptor.capture(),
                receivedReasonCaptor.capture(), toAddressCaptor.capture());
        assertThat(receivedReasonCaptor.getValue()).contains(
                "You are a team member in the \"de\" language team");
        assertThat(toAddressCaptor.getValue().toString()).isEqualTo(
                "John Smith <john@a.c>");
        LanguageTeamPermissionChangeEmailStrategy strategy =
                strategyArgumentCaptor.getValue();
        assertThat(strategy.getSubject(new Messages(Locale.ENGLISH))).isEqualTo(
                "Your permissions in language team \"de\" have changed");

    }
}
