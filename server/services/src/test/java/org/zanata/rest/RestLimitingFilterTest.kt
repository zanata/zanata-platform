package org.zanata.rest

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
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.zanata.ZanataTest
import org.zanata.limits.RateLimitingProcessor
import org.zanata.model.HAccount
import org.zanata.test.CdiUnitRunner
import org.zanata.util.HttpUtil
import org.zanata.util.RunnableEx

import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

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

    private val request = mock<HttpServletRequest>(RETURNS_DEEP_STUBS)
    private val response = mock<HttpServletResponse>(RETURNS_DEEP_STUBS)
    private final val processor = mock<RateLimitingProcessor>()
    private val filterChain = mock<FilterChain>()
    private val taskCaptor = captor<RunnableEx>()

    private final var authenticatedUser: HAccount? = null
    // Using @Inject would be better, but some tests currently require
    // authenticatedUser to be null, which is difficult without spy()
    private val filter: RestLimitingFilter =
            spy(RestLimitingFilter(processor, authenticatedUser))

    @Before
    fun beforeMethod() {
        `when`(request.method).thenReturn("GET")

        // this way we can verify the task actually called super.invoke()
        doNothing().`when`<FilterChain>(filterChain).doFilter(request, response)
    }

    @Test
    fun willUseApiKeyIfPresent() {
        `when`(request.getHeader(HttpUtil.API_KEY_HEADER_NAME)).thenReturn(
                API_KEY)

        filter.doFilter(request, response, filterChain)

        verify<RateLimitingProcessor>(processor).processForApiKey(same(API_KEY), same(response),
                taskCaptor.capture())

        // verify task is calling filter chain
        val task = taskCaptor.value
        task.run()
        verify<FilterChain>(filterChain).doFilter(request, response)
    }

    @Test
    fun willUseUsernameIfNoApiKeyButAuthenticated() {
        authenticatedUser = HAccount()
        authenticatedUser!!.username = "admin"
        doReturn(authenticatedUser).`when`<RestLimitingFilter>(filter).authenticatedUser

        filter.doFilter(request, response, filterChain)

        verify<RateLimitingProcessor>(processor).processForUser(same("admin"), same(response),
                taskCaptor.capture())

        // verify task is calling filter chain
        val task = taskCaptor.value
        task.run()
        verify<FilterChain>(filterChain).doFilter(request, response)
    }

    @Test
    fun willUseAuthorizationCodeIfItPresents() {
        val authCode = "abc123"
        `when`(request.getParameter(OAuth.OAUTH_CODE)).thenReturn(
                authCode)

        filter.doFilter(request, response, filterChain)

        verify<RateLimitingProcessor>(processor).processForToken(same(authCode), same(response),
                taskCaptor.capture())

        // verify task is calling filter chain
        val task = taskCaptor.value
        task.run()
        verify<FilterChain>(filterChain).doFilter(request, response)
    }

    @Test
    fun willUseAccessTokenIfItPresents() {
        `when`(request.getHeader(OAuth.HeaderType.AUTHORIZATION)).thenReturn(
                "Bearer abc123")

        filter.doFilter(request, response, filterChain)

        verify<RateLimitingProcessor>(processor).processForToken(ArgumentMatchers.eq("abc123"), same(response),
                taskCaptor.capture())

        // verify task is calling filter chain
        val task = taskCaptor.value
        task.run()
        verify<FilterChain>(filterChain).doFilter(request, response)
    }

    @Test
    fun willProcessAnonymousWithGETAndNoApiKey() {
        `when`(request.getHeader(HttpUtil.API_KEY_HEADER_NAME)).thenReturn(null)
        `when`(request.requestURI).thenReturn("/rest/in/peace")
        `when`(request.remoteAddr).thenReturn(clientIP)
        doReturn(null).`when`<RestLimitingFilter>(filter).authenticatedUser

        filter.doFilter(request, response, filterChain)

        verify<RateLimitingProcessor>(processor).processForAnonymousIP(same(clientIP), same(response),
                taskCaptor.capture())

        // verify task is calling filter chain
        val task = taskCaptor.value
        task.run()
        verify<FilterChain>(filterChain).doFilter(request, response)
    }

}

// borrowed from https://github.com/nhaarman/mockito-kotlin
// TODO use mockito-kotlin?
inline fun <reified T : Any> mock(): T = Mockito.mock(T::class.java)!!
inline fun <reified T : Any> mock(defaultAnswer: Answer<Any>): T = Mockito.mock(T::class.java, defaultAnswer)!!
inline fun <reified T : Any> captor(): ArgumentCaptor<T> = ArgumentCaptor.forClass(T::class.java)
