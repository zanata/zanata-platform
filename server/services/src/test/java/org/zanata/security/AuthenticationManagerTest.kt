package org.zanata.security

import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.jglue.cdiunit.InRequestScope
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.zanata.ApplicationConfiguration
import org.zanata.dao.AccountDAO
import org.zanata.dao.CredentialsDAO
import org.zanata.events.LoginCompleted
import org.zanata.i18n.Messages
import org.zanata.model.HAccount
import org.zanata.model.security.HSaml2Credentials
import org.zanata.seam.security.ZanataJpaIdentityStore
import org.zanata.security.annotations.SAML
import org.zanata.service.UserAccountService
import org.zanata.test.CdiUnitRunner
import org.zanata.ui.faces.FacesMessages
import javax.enterprise.inject.Produces
import javax.inject.Inject

@RunWith(CdiUnitRunner::class)
class AuthenticationManagerTest {
    @Produces
    @Mock
    private lateinit var identity: ZanataIdentity

    @Produces
    @Mock
    private lateinit var identityStore: ZanataJpaIdentityStore
    @Produces
    @Mock
    private lateinit var credentials: ZanataCredentials
    @Produces
    @Mock
    private lateinit var zanataOpenId: ZanataOpenId
    @Produces
    @Mock
    private lateinit var facesMessages: FacesMessages
    @Produces
    @Mock
    private lateinit var userAccountServiceImpl: UserAccountService
    @Produces
    @Mock
    private lateinit var credentialsDAO: CredentialsDAO
    @Produces
    @Mock
    private lateinit var accountDAO: AccountDAO
    @Produces
    @Mock
    private lateinit var userRedirect: UserRedirectBean
    @Produces
    @Mock
    private lateinit var applicationConfiguration: ApplicationConfiguration
    @Produces
    @Mock
    private lateinit var msgs: Messages
    @Produces
    @Mock
    private lateinit var spNegoIdentity: SpNegoIdentity
    @Produces
    @SAML
    private var saml2Enabled: Boolean = true
    @Produces
    @Mock
    private lateinit var samlIdentity: SamlIdentity
    @Inject
    private lateinit var authenticationManager: AuthenticationManager
    @Mock
    @Produces private lateinit var samlAccountService: SamlAccountService

    @Test
    @InRequestScope
    fun onExternalLoginForExistingUser() {
        given(identity.credentials).willReturn(credentials)
        val username = "admin"
        given(credentials.username).willReturn(username)
        given(identityStore.isUserEnabled(username)).willReturn(true)
        given(identityStore.getImpliedRoles(username)).willReturn(listOf("user"))
        val uniqueId = "abc-1234"
        given(samlIdentity.uniqueName).willReturn(uniqueId)
        val theAccount = HAccount()
        val samlCredentials = HSaml2Credentials(theAccount, uniqueId, "admin@example.com")
        given(credentialsDAO.findByUser(uniqueId)).willReturn(samlCredentials)
        given(identityStore.lookupUser(username)).willReturn(theAccount)

        authenticationManager.onLoginCompleted(LoginCompleted(AuthenticationType.SAML2))

        verify(identity).addRole("user")
        verify(identityStore).setAuthenticateUser(theAccount)
        verify(userAccountServiceImpl).runRoleAssignmentRules(theAccount, samlCredentials, AuthenticationType.SAML2.name)
    }

    @Test
    @InRequestScope
    fun onExternalLoginForNewUser() {
        given(identity.credentials).willReturn(credentials)
        val username = "admin"
        given(credentials.username).willReturn(username)
        given(identityStore.isUserEnabled(username)).willReturn(false)
        given(identityStore.isNewUser(username)).willReturn(true)

        authenticationManager.onLoginCompleted(LoginCompleted(AuthenticationType.SAML2))

        verify(identity, never()).addRole(anyString())
    }

    @Test
    @InRequestScope
    fun onSSOLogin() {
        given(identity.credentials).willReturn(credentials)
        val username = "admin"
        given(credentials.username).willReturn(username)
        given(identityStore.isUserEnabled(username)).willReturn(true)

        authenticationManager.ssoLogin()

        verify(samlIdentity).authenticate()
        verify(samlAccountService).tryMergeToExistingAccount()
    }
}
