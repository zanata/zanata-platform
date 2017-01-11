package org.zanata.action;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccountResetPasswordKey;
import org.zanata.seam.security.IdentityManager;
import org.zanata.service.EmailService;
import org.zanata.service.RegisterService;
import org.zanata.service.UserAccountService;
import org.zanata.ui.faces.FacesMessages;

import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdminCreateUserActionTest {
    @Mock private IdentityManager identityManager;
    @Mock private FacesMessages facesMessages;
    private Messages msgs;
    @Mock private EmailService emailService;
    @Mock private RegisterService registerService;
    @Mock private UserAccountService userAccountService;
    private AdminCreateUserAction action;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        msgs = new Messages(Locale.ENGLISH);
        action = new AdminCreateUserAction(identityManager, facesMessages,
                msgs, emailService, registerService, userAccountService);
    }

    @Test
    public void saveWillFailIfUsernameIsAlreadyTaken() {
        action.setUsername("tester");
        when(userAccountService.isUsernameUsed("tester")).thenReturn(true);

        String result = action.saveNewUser();

        assertThat(result).isEqualTo("failure");
    }

    @Test
    public void saveWillSucceedIfUsernameIsAvailable() {
        String username = "tester";
        String email = "tester@example.com";
        List<String> roles = Lists.newArrayList("user", "project-creator");
        action.setUsername(username);
        action.setEmail(email);
        action.setRoles(roles);
        when(userAccountService.isUsernameUsed(username)).thenReturn(false);
        String activationKey = "activationKey";
        when(registerService.register(eq(username), anyString(), eq(username), eq(email))).thenReturn(
                activationKey);
        HAccountResetPasswordKey resetPasswordKey = new HAccountResetPasswordKey();
        String resetPasswordKeyHash = "resetPasswordKey";
        resetPasswordKey.setKeyHash(resetPasswordKeyHash);
        when(userAccountService.requestPasswordReset(username, email)).thenReturn(
                resetPasswordKey);

        String result = action.saveNewUser();

        assertThat(result).isEqualTo("success");
        verify(emailService).sendActivationAndResetPasswordEmail(username, email,
                activationKey, resetPasswordKeyHash);
    }


}
