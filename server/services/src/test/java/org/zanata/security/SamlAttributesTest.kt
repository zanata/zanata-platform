package org.zanata.security

import io.undertow.security.idm.Account
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.picketlink.common.constants.GeneralConstants
import org.picketlink.identity.federation.bindings.wildfly.sp.SPFormAuthenticationMechanism
import javax.servlet.http.HttpSession

class SamlAttributesTest {
    private lateinit var samlAttributes: SamlAttributes

    @Mock
    private lateinit var session: HttpSession
    @Mock
    private lateinit var account: Account

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        samlAttributes = SamlAttributes(session)
    }

    @Test
    fun isNotAuthenticatedIfSessionContainsNoAccount() {
        given(session.getAttribute(
                SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE)).willReturn(null)

        assertThat(samlAttributes.isSessionAuthenticatedBySAML()).isFalse()

    }

    @Test
    fun `is not authenticated if account in session contains no "authenticated" role`() {

        // roles is empty. e.g. not containing "authenticated" role
        given(account.roles).willReturn(setOf())
        given(session.getAttribute(
                SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE)).willReturn(account)

        assertThat(samlAttributes.isSessionAuthenticatedBySAML()).isFalse()
    }

    @Test
    fun `is authenticated if session contains account and has role "authenticated"`() {
        given(account.roles).willReturn(setOf("authenticated"))
        given(session.getAttribute(
                SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE)).willReturn(account)
        given(account.principal).willReturn(SimplePrincipal("jsmith"))

        assertThat(samlAttributes.isSessionAuthenticatedBySAML()).isTrue()
    }

    @Test
    fun canGetPrincipal() {
        val simplePrincipal = SimplePrincipal("jsmith")
        given(account.principal).willReturn(simplePrincipal)
        given(account.roles).willReturn(setOf("authenticated"))
        given(session.getAttribute(SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE)).willReturn(account)

        val principal = samlAttributes.principalFromSAMLResponse()
        assertThat(principal).isNotNull()
        assertThat(principal).isSameAs(simplePrincipal)
    }

    @Test
    fun canGetUsername() {
        given(session.getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP))
                .willReturn(mapOf("uid" to listOf("jsmith")))
        samlAttributes = SamlAttributes(session)

        val username = samlAttributes.usernameFromSAMLResponse(SimplePrincipal("abc-123-unique"))

        assertThat(username).isEqualTo("jsmith")
    }

    @Test
    fun willUsePrincipalNameIfUsernameIsNull() {
        given(session.getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP))
                .willReturn(emptyMap<String, List<String>>())
        samlAttributes = SamlAttributes(session)

        val username = samlAttributes.usernameFromSAMLResponse(SimplePrincipal("abc-123-unique"))

        assertThat(username).isEqualTo("abc-123-unique")
    }

    @Test
    fun canGetCommonName() {
        given(session.getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP))
                .willReturn(mapOf("cn" to listOf("Joe Smith")))
        samlAttributes = SamlAttributes(session)

        val name = samlAttributes.commonNameFromSAMLResponse(SimplePrincipal("abc-123-unique"))

        assertThat(name).isEqualTo("Joe Smith")
    }

    @Test
    fun canGetEmail() {
        given(session.getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP))
                .willReturn(mapOf("email" to listOf("jsmith@example.com")))
        samlAttributes = SamlAttributes(session)

        val email = samlAttributes.emailFromSAMLResponse(SimplePrincipal("abc-123-unique"))

        assertThat(email).isEqualTo("jsmith@example.com")
    }

}
