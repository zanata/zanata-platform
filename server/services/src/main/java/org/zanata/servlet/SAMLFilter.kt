/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.servlet

import com.google.common.annotations.VisibleForTesting
import org.zanata.security.AuthenticationManager
import org.zanata.security.SamlAccountService
import org.zanata.security.SamlAttributes
import org.zanata.util.UrlUtil
import java.io.IOException
import javax.inject.Inject
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Patrick Huang
 * *         [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@WebFilter(filterName = "ssoFilter")
class SAMLFilter() : Filter {
    @Inject
    private lateinit var authenticationManager: AuthenticationManager
    @Inject
    private lateinit var urlUtil: UrlUtil
    @Inject
    private lateinit var samlAttributes: SamlAttributes
    @Inject
    private lateinit var samlAccountService: SamlAccountService

    @VisibleForTesting
    constructor(authenticationManager: AuthenticationManager, urlUtil: UrlUtil,
                samlAttributes: SamlAttributes, samlAccountService: SamlAccountService) : this() {
        this.authenticationManager = authenticationManager
        this.urlUtil = urlUtil
        this.samlAttributes = samlAttributes
        this.samlAccountService = samlAccountService
    }

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {}

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse,
                          chain: FilterChain) {
        if (request is HttpServletRequest) {
            if (samlAttributes.isAuthenticated) {
                authenticationManager.ssoLogin()
                // here we try to auto merge account if email matches
                samlAccountService.tryMergeToExistingAccount()
                performRedirection(response as HttpServletResponse)
                return
            }
        }
        chain.doFilter(request, response)
    }

    override fun destroy() {}

    @Throws(IOException::class)
    private fun performRedirection(resp: HttpServletResponse) {
        // Performs the redirection based on the results from the authentication
        // process.
        // This is logic that would normally be in faces-config.xml, but as this is
        // a servlet filter, it cannot take advantage of that.
        val authRedirectResult = authenticationManager.authenticationRedirect
        when (authRedirectResult) {
            "login" -> resp.sendRedirect(urlUtil.signInPage())
            "edit" -> resp.sendRedirect(urlUtil.createUserPage())
            "inactive" -> resp.sendRedirect(urlUtil.inactiveAccountPage())
            "dashboard" -> resp.sendRedirect(urlUtil.dashboardUrl())
            "redirect" ->
                // FIXME sso won't preserve continue parameter yet. We just send to dashboard
                resp.sendRedirect(urlUtil.dashboardUrl())
            "home" -> resp.sendRedirect(urlUtil.home())
            else -> throw RuntimeException(
                    "Unexpected authentication manager result: " + authRedirectResult)
        }
    }
}
