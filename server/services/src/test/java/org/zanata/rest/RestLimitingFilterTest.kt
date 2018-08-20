package org.zanata.rest

import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore
import org.apache.oltu.oauth2.common.OAuth
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers.RETURNS_DEEP_STUBS
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.same
import org.zanata.ZanataTest
import org.zanata.limits.RateLimitingProcessor
import org.zanata.model.HAccount
import org.zanata.test.CdiUnitRunner
import org.zanata.util.HttpUtil
import org.zanata.util.RunnableEx

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@RunWith(CdiUnitRunner::class)
@SupportDeltaspikeCore
class RestLimitingFilterTest : ZanataTest() {

    companion object {
        private val API_KEY = "apiKey123"
        private val clientIP = "255.255.0.1"
    }

    private val request: HttpServletRequest = mock(defaultAnswer = RETURNS_DEEP_STUBS)
    private val response: HttpServletResponse = mock(defaultAnswer = RETURNS_DEEP_STUBS)
    private final val processor: RateLimitingProcessor = mock()
    private val filterChain: FilterChain = mock()
    private val taskCaptor = captor<RunnableEx>()

    private final var authenticatedUser: HAccount? = null
    // Using @Inject would be better, but some tests currently require
    // authenticatedUser to be null, which is difficult without spy()
    private val filter: RestLimitingFilter =
            spy(RestLimitingFilter(processor, authenticatedUser))

    @Before
    fun beforeMethod() {
        whenever(request.method).thenReturn("GET")

        // this way we can verify the task actually called super.invoke()
        doNothing().whenever(filterChain).doFilter(request, response)
    }

    @Test
    fun willUseApiKeyIfPresent() {
        whenever(request.getHeader(HttpUtil.API_KEY_HEADER_NAME)).thenReturn(
                API_KEY)

        filter.doFilter(request, response, filterChain)

        verify(processor).processForApiKey(same(API_KEY), same(response),
                taskCaptor.capture())

        // verify task is calling filter chain
        val task = taskCaptor.value
        task.run()
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun willUseUsernameIfNoApiKeyButAuthenticated() {
        authenticatedUser = HAccount()
        authenticatedUser!!.username = "admin"
        doReturn(authenticatedUser).whenever<RestLimitingFilter>(filter).authenticatedUser

        filter.doFilter(request, response, filterChain)

        verify(processor).processForUser(same("admin"), same(response),
                taskCaptor.capture())

        // verify task is calling filter chain
        val task = taskCaptor.value
        task.run()
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun willUseAuthorizationCodeIfItPresents() {
        val authCode = "abc123"
        whenever(request.getParameter(OAuth.OAUTH_CODE)).thenReturn(
                authCode)

        filter.doFilter(request, response, filterChain)

        verify(processor).processForToken(same(authCode), same(response),
                taskCaptor.capture())

        // verify task is calling filter chain
        val task = taskCaptor.value
        task.run()
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun willUseAccessTokenIfItPresents() {
        whenever(request.getHeader(OAuth.HeaderType.AUTHORIZATION)).thenReturn(
                "Bearer abc123")

        filter.doFilter(request, response, filterChain)

        verify(processor).processForToken(ArgumentMatchers.eq("abc123"), same(response),
                taskCaptor.capture())

        // verify task is calling filter chain
        val task = taskCaptor.value
        task.run()
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun willProcessAnonymousWithGETAndNoApiKey() {
        whenever(request.getHeader(HttpUtil.API_KEY_HEADER_NAME)).thenReturn(null)
        whenever(request.requestURI).thenReturn("/rest/in/peace")
        whenever(request.remoteAddr).thenReturn(clientIP)
        doReturn(null).whenever(filter).authenticatedUser

        filter.doFilter(request, response, filterChain)

        verify(processor).processForAnonymousIP(same(clientIP), same(response),
                taskCaptor.capture())

        // verify task is calling filter chain
        val task = taskCaptor.value
        task.run()
        verify(filterChain).doFilter(request, response)
    }

}

// borrowed from https://github.com/nhaarman/mockito-kotlin
// TODO use argumentCaptor?
inline fun <reified T : Any> captor(): ArgumentCaptor<T> = ArgumentCaptor.forClass(T::class.java)
