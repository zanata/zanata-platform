package org.zanata.security

import io.undertow.security.idm.Account
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.picketlink.common.constants.GeneralConstants
import org.picketlink.identity.federation.bindings.wildfly.sp.SPFormAuthenticationMechanism
import javax.servlet.http.HttpSession

class SamlLoginTest {
    private lateinit var samlLogin: SamlLogin

    @Mock
    private lateinit var session: HttpSession
    @Mock
    private lateinit var account: Account

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        samlLogin = SamlLogin(session)
    }

    @Test
    fun isNotAuthenticatedIfSessionContainsNoAccount() {
        BDDMockito.given(session.getAttribute(
                SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE)).willReturn(null)

        assertThat(samlLogin.isAuthenticatedExternally()).isFalse()

    }

    @Test
    fun willDoNothingIfAccountInSessionContainsNoAuthenticatedRole() {

        // roles is empty. e.g. not containing "authenticated" role
        BDDMockito.given(account.roles).willReturn(setOf())
        BDDMockito.given(session.getAttribute(
                SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE)).willReturn(account)

        assertThat(samlLogin.isAuthenticatedExternally()).isFalse()
    }

    @Test
    fun isAuthenticatedIfSessionContainsAccountAndHasRoleAuthenticated() {
        BDDMockito.given(account.roles).willReturn(setOf("authenticated"))
        BDDMockito.given(session.getAttribute(
                SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE)).willReturn(account)
        BDDMockito.given(account.principal).willReturn(SimplePrincipal("jsmith"))

        assertThat(samlLogin.isAuthenticatedExternally()).isTrue()
    }

    @Test
    fun canGetPrincipal() {
        val simplePrincipal = SimplePrincipal("jsmith")
        BDDMockito.given(account.principal).willReturn(simplePrincipal)
        BDDMockito.given(account.roles).willReturn(setOf("authenticated"))
        BDDMockito.given(session.getAttribute(SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE)).willReturn(account)

        val principal = samlLogin.principalFromSAMLResponse()
        assertThat(principal).isNotNull()
        assertThat(principal).isSameAs(simplePrincipal)
    }
    
    @Test
    fun canGetUsername() {
        BDDMockito.given(session.getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP))
                .willReturn(mapOf("uid" to listOf("jsmith")))
        samlLogin = SamlLogin(session)

        val username = samlLogin.usernameFromSAMLResponse(SimplePrincipal("abc-123-unique"))

        assertThat(username).isEqualTo("jsmith")
    }

    @Test
    fun willUsePrincipalNameIfUsernameIsNull() {
        BDDMockito.given(session.getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP))
                .willReturn(emptyMap<String, List<String>>())
        samlLogin = SamlLogin(session)

        val username = samlLogin.usernameFromSAMLResponse(SimplePrincipal("abc-123-unique"))

        assertThat(username).isEqualTo("abc-123-unique")
    }

    @Test
    fun canGetCommonName() {
        BDDMockito.given(session.getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP))
                .willReturn(mapOf("cn" to listOf("Joe Smith")))
        samlLogin = SamlLogin(session)

        val name = samlLogin.commonNameFromSAMLResponse(SimplePrincipal("abc-123-unique"))

        assertThat(name).isEqualTo("Joe Smith")
    }

    @Test
    fun canGetEmail() {
        BDDMockito.given(session.getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP))
                .willReturn(mapOf("email" to listOf("jsmith@example.com")))
        samlLogin = SamlLogin(session)

        val email = samlLogin.emailFromSAMLResponse(SimplePrincipal("abc-123-unique"))

        assertThat(email).isEqualTo("jsmith@example.com")
    }

}