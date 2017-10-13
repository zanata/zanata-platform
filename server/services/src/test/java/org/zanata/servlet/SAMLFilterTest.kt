package org.zanata.servlet

import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.zanata.security.AuthenticationManager
import org.zanata.security.SamlAttributes
import org.zanata.util.UrlUtil
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SAMLFilterTest {
    lateinit var filter : SAMLFilter
    @Mock
    private lateinit var chain: FilterChain
    @Mock
    private lateinit var request: HttpServletRequest
    @Mock
    private lateinit var response: HttpServletResponse
    @Mock
    private lateinit var samlAttributes: SamlAttributes
    @Mock
    private lateinit var authenticationManager: AuthenticationManager
    @Mock
    private lateinit var urlUtil: UrlUtil


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        filter = SAMLFilter(authenticationManager, urlUtil, samlAttributes)
    }


    @Test
    fun willNotRedirectIfItIsNotHttpServletRequest() {
        val request: ServletRequest = Mockito.mock(ServletRequest::class.java)
        val response: ServletResponse = Mockito.mock(ServletResponse::class.java)
        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
        verifyNoMoreInteractions(request, response)
    }

    @Test
    fun willNotRedirectIfItIsNotAuthenticated() {
        given(samlAttributes.isAuthenticated).willReturn(false)

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
        verifyZeroInteractions(authenticationManager)
        verify(response, never()).sendRedirect(anyString())
    }

    @Test
    fun willRedirectToPageIfItIsAuthenticated() {
        given(samlAttributes.isAuthenticated).willReturn(true)

        given(authenticationManager.authenticationRedirect).willReturn("dashboard")
        given(urlUtil.dashboardUrl()).willReturn("/dashboard")

        filter.doFilter(request, response, chain)

        verify(response).sendRedirect("/dashboard")
    }
}
