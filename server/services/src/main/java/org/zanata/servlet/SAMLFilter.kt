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
import io.undertow.security.idm.Account
import org.picketlink.common.constants.GeneralConstants
import org.picketlink.identity.federation.bindings.wildfly.sp.SPFormAuthenticationMechanism
import org.slf4j.LoggerFactory
import org.zanata.security.AuthenticationManager
import org.zanata.util.UrlUtil
import java.io.IOException
import javax.inject.Inject
import javax.servlet.*
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
    lateinit private var authenticationManager: AuthenticationManager
    @Inject
    lateinit private var urlUtil: UrlUtil

    @VisibleForTesting
    constructor(authenticationManager: AuthenticationManager, urlUtil: UrlUtil) : this() {
        this.authenticationManager = authenticationManager
        this.urlUtil = urlUtil
    }


    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {}

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse,
                          chain: FilterChain) {
        if (request is HttpServletRequest) {
            val session = request.session
            val account: Account? = session.getAttribute(
                    SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE) as? Account
            if (account != null && account.roles.contains("authenticated")) {
                val principalName = account.principal.name

                val samlAttributeMap: Map<String, List<String>?> =
                        session.getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP) as? Map<String, List<String>?>? ?: mapOf()
                // These assumes IDP follow standard SAML assertion names
                val usernameFromSSO = getValueFromSessionAttribute(samlAttributeMap, "uid", { _ -> principalName})
                val emailFromSSO = getValueFromSessionAttribute(samlAttributeMap, "email")
                val nameFromSSO = getValueFromSessionAttribute(samlAttributeMap, "cn")
                log.info("SSO login: username: {}, name: {}, uuid: {}",
                        usernameFromSSO, nameFromSSO, principalName)
                authenticationManager.ssoLogin(account,
                        usernameFromSSO, emailFromSSO, nameFromSSO)
                performRedirection(response as HttpServletResponse)
                return
            }
        }
        chain.doFilter(request, response)
    }


    override fun destroy() {}

    /**
     * Performs the redirection based on the results from the authentication
     * process.
     * This is logic that would normally be in faces-config.xml, but as this is
     * a servlet filter, it cannot take advantage of that.
     */
    @Throws(IOException::class)
    private fun performRedirection(resp: HttpServletResponse) {
        val authRedirectResult = authenticationManager.authenticationRedirect
        when (authRedirectResult) {
            "login" -> resp.sendRedirect(urlUtil.signInPage())
            "edit" -> resp.sendRedirect(urlUtil.createUserPage())
            "inactive" -> resp.sendRedirect(urlUtil.inactiveAccountPage())
            "dashboard" -> resp.sendRedirect(urlUtil.dashboardUrl())
            "redirect" ->
                // sso should not have any continue url. We just send to dashboard
                resp.sendRedirect(urlUtil.dashboardUrl())
            "home" -> resp.sendRedirect(urlUtil.home())
            else -> throw RuntimeException(
                    "Unexpected authentication manager result: " + authRedirectResult)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SAMLFilter::class.java)

        private fun getValueFromSessionAttribute(map: Map<String, List<String>?>, key: String, defaultVal : (Int) -> String = {_ -> ""}) =
            map[key]?.elementAtOrElse(0, defaultVal)
    }
}
