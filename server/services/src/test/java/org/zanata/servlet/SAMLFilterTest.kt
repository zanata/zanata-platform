package org.zanata.servlet

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import io.undertow.security.idm.Account
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.picketlink.common.constants.GeneralConstants.SESSION_ATTRIBUTE_MAP
import org.picketlink.identity.federation.bindings.wildfly.sp.SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE
import org.zanata.security.AuthenticationManager
import org.zanata.security.SimplePrincipal
import org.zanata.util.UrlUtil
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

class SAMLFilterTest {
    lateinit var filter : SAMLFilter
    @Mock
    lateinit private var chain: FilterChain
    @Mock
    lateinit private var request: HttpServletRequest
    @Mock
    lateinit private var response: HttpServletResponse
    @Mock
    lateinit private var session: HttpSession
    @Mock
    lateinit private var account: Account
    @Mock
    lateinit private var authenticationManager: AuthenticationManager
    @Mock
    lateinit private var urlUtil: UrlUtil


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        filter = SAMLFilter()
    }


    @Test
    fun willDoNothingNotHttpServletRequest() {
        val request: ServletRequest = Mockito.mock(ServletRequest::class.java)
        val response: ServletResponse = Mockito.mock(ServletResponse::class.java)
        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
        verifyNoMoreInteractions(request, response)
    }

    @Test
    fun willDoNothingIfSessionContainsNoAccount() {
        given(request.session).willReturn(session)
        given(session.getAttribute(
                FORM_ACCOUNT_NOTE)).willReturn(null)

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
        verify(request).session
        verify(session).getAttribute(FORM_ACCOUNT_NOTE)
        verifyNoMoreInteractions(request, session, response)
    }

    @Test
    fun willDoNothingIfAccountInSessionContainsNoAuthenticatedRole() {

        given(request.session).willReturn(session)
        // roles is empty. e.g. not containing "authenticated" role
        given(account.roles).willReturn(setOf())
        given(session.getAttribute(
                FORM_ACCOUNT_NOTE)).willReturn(account)

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
        verify(request).session
        verify(session).getAttribute(FORM_ACCOUNT_NOTE)
        verifyNoMoreInteractions(request, session, response)
    }

    @Test
    fun willAuthenticateIfSessionHasAuthenticatedAccount() {
        filter = SAMLFilter(authenticationManager, urlUtil)
        given(request.session).willReturn(session)
        given(account.principal).willReturn(SimplePrincipal("jsmith"))
        given(account.roles).willReturn(setOf("authenticated"))
        given(session.getAttribute(FORM_ACCOUNT_NOTE)).willReturn(account)
        given(session.getAttribute(SESSION_ATTRIBUTE_MAP))
                .willReturn(mapOf(
                        "uid" to listOf("abc-123-unique"),
                        "email" to listOf("jsmith@example.com"),
                        "cn" to listOf("John Smith")
                ))
        given(authenticationManager.authenticationRedirect).willReturn("dashboard")
        given(urlUtil.dashboardUrl()).willReturn("/dashboard")

        filter.doFilter(request, response, chain)

        verify(authenticationManager).ssoLogin(account, "abc-123-unique", "jsmith@example.com", "John Smith")
        verify(response).sendRedirect("/dashboard")
    }
}