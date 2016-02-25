package org.zanata.security;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ZanataJpaTest;
import org.zanata.exception.AuthorizationException;
import org.zanata.exception.NotLoggedInException;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HProjectIteration;
import org.zanata.seam.AutowireContexts;
import org.zanata.seam.SeamAutowire;
import org.zanata.seam.security.IdentityManager;
import org.zanata.security.permission.CustomPermissionResolver;
import org.zanata.security.permission.PermissionEvaluator;

import javax.enterprise.event.Event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.zanata.util.PasswordUtil.generateSaltedHash;

public class ZanataIdentityTest extends ZanataJpaTest {
    private static final SeamAutowire seam = SeamAutowire.instance();
    private static final String username = "translator";
    private static final String apiKey = "d83882201764f7d339e97c4b087f0806";
    private static final String validPassword = "translator";
    private static boolean securityEnabled;
    private ZanataIdentity identity;
    @Mock
    private Event event;
    @Mock
    private IdentityManager identityManager;
    private HAccount account;
    //    private CustomPermissionResolver permissionResolver;

    @BeforeClass
    public static void setUpEnvironment() {
        seam.simulateSessionContext(true).simulateEventContext(true);
        securityEnabled = ZanataIdentity.isSecurityEnabled();
        ZanataIdentity.setSecurityEnabled(true);
    }

    @AfterClass
    public static void cleanUp() {
        seam.simulateEventContext(false).simulateSessionContext(false);
        ZanataIdentity.setSecurityEnabled(securityEnabled);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        deleteAllTables();
        getEm().flush();
        ZanataCredentials credentials = new ZanataCredentials();
        PermissionEvaluator permissionEvaluator = new PermissionEvaluator();
        when(identityManager.isEnabled()).thenReturn(true);
        when(identityManager.authenticate(username, validPassword)).thenReturn(true);
        when(identityManager.authenticate(username, apiKey)).thenReturn(true);

        identity = seam
                .reset().ignoreNonResolvable()
                .use("credentials", credentials)
                .use("entityManager", getEm())
                .use("identityManager", identityManager)
                .use("loginSuccessfulEventEvent", event)
                .use("logoutEvent", event)
                .use("alreadyLoggedInEventEvent", event)
                .use("loginFailedEventEvent", event)
                .use("notLoggedInEventEvent", event)
                .use("session", getSession())
                .use("serverPath", "/")
                .use("windowContext", new WindowContext() {
                    @Override
                    public String getCurrentWindowId() {
                        return "dummyWindowId";
                    }
                    @Override
                    public void activateWindow(String windowId) {
                    }
                    @Override
                    public boolean closeWindow(String windowId) {
                        return false;
                    }
                })
                .autowire(ZanataIdentity.class);
        seam.use("identity", identity);
        seam.use("permissionProviders", seam.autowire(SecurityFunctions.class))
            .use("permissionEvaluator", seam.autowire(permissionEvaluator));
        identity.setJaasConfigName(null);
        identity.setPermissionResolver(new CustomPermissionResolver());
        AutowireContexts.simulateSessionContext(true);
        account = makeAccount();
        getEm().persist(account);
    }


    private static HAccount makeAccount() {
        HAccount account = new HAccount();
        account.setUsername(username);
        account.setPasswordHash(
                generateSaltedHash(validPassword, username));
        account.setApiKey(apiKey);
        account.setEnabled(true);
        return account;
    }

    @Test
    public void canLogin() {
        identity.getCredentials().setUsername(username);
        identity.getCredentials().setPassword(validPassword);
        String login = identity.login();
        assertThat(login).isEqualTo("loggedIn");
        assertThat(identity.isLoggedIn()).isTrue();
    }

    @Test
    public void invalidPassword() {
        identity.getCredentials().setUsername(username);
        identity.getCredentials().setPassword("invalid password");
        String login = identity.login();
        assertThat(login).isNull();
    }

    @Test
    public void canAddRole() {
        assertThat(identity.addRole("admin")).isFalse()
                .as("before login addRole will not be successful");
        assertThat(identity.hasRole("admin")).isFalse()
                .as("before login hasRole is always false");

        identity.getCredentials().setUsername(username);
        identity.getCredentials().setPassword(validPassword);
        identity.login();

        assertThat(identity.hasRole("admin")).isFalse();

        assertThat(identity.addRole("admin")).isTrue()
                .as("after login addRole can be done");

        assertThat(identity.hasRole("admin")).isTrue();
        identity.checkRole("admin"); // checkRole will not cause an exception
        assertThat(identity.hasRole("user")).isFalse();
    }

    @Test(expected = NotLoggedInException.class)
    public void checkLoggedInWillThrowIfNotLoggedIn() {
        identity.checkLoggedIn();
    }

    @Test
    public void checkLoggedInDoesNotThrowIfLoggedIn() {
        identity.getCredentials().setUsername(username);
        identity.getCredentials().setPassword(validPassword);
        identity.login();
        identity.checkLoggedIn();
    }

    @Test(expected = AuthorizationException.class)
    public void checkRoleWillThrowIfDoesNotHaveTheRole() {
        identity.getCredentials().setUsername(username);
        identity.getCredentials().setPassword(validPassword);
        identity.login();
        identity.checkRole("admin");
    }

    @Test
    public void canAcceptExternallyAuthenticatedPrincipalForOpenId() {
        SimplePrincipal principal = new SimplePrincipal("user");
        identity.acceptExternallyAuthenticatedPrincipal(principal);
        assertThat(identity.isLoggedIn()).isTrue();
    }

    @Test
    public void canLoginUsingApiKey() {
        identity.getCredentials().setUsername("translator");
        identity.setApiKey(apiKey);
        identity.login();
        assertThat(identity.isLoggedIn()).isTrue();
    }

    @Test
    public void canNotLogInIfApiKeyIsWrong() {
        identity.getCredentials().setUsername("translator");
        identity.setApiKey("invalid_api_token");
        identity.login();
        assertThat(identity.isLoggedIn()).isFalse();
    }

    @Test
    public void canTestPermission() {
        HAccountRole target = new HAccountRole();
        target.setName("user");
        assertThat(identity.hasPermission(target, "seam.insert")).isFalse()
                .as("only admin can create role");

        identity.getCredentials().setUsername(username);
        identity.getCredentials().setPassword(validPassword);
        identity.login();

        boolean addedUser = identity.addRole("user");
        assert addedUser;

        assertThat(identity.hasPermission(target, "seam.insert")).isFalse()
                .as("ordinary user do not have permission to create role");

        boolean addedAdmin = identity.addRole("admin");
        assert addedAdmin;

        assertThat(identity.hasPermission(target, "seam.insert")).isTrue();
    }

    @Test(expected = AuthorizationException.class)
    public void canCheckPermission() {
        identity.getCredentials().setUsername(username);
        identity.getCredentials().setPassword(validPassword);
        identity.login();

        identity.checkPermission(new HProjectIteration(), "importTranslation");
    }
}
