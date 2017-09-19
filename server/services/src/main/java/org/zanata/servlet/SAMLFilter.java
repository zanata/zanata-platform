package org.zanata.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.identity.federation.bindings.wildfly.sp.SPFormAuthenticationMechanism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.security.AuthenticationManager;
import org.zanata.util.UrlUtil;

import io.undertow.security.idm.Account;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@WebFilter(filterName = "ssoFilter")
public class SAMLFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(SAMLFilter.class);
    @Inject
    private AuthenticationManager authenticationManager;
    @Inject
    private UrlUtil urlUtil;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest r = (HttpServletRequest) request;
            Object account = r.getSession().getAttribute(
                    SPFormAuthenticationMechanism.FORM_ACCOUNT_NOTE);
            if (account != null && account instanceof Account) {
                Account acc = (Account) account;
                Optional<Map<String, List<String>>> samlAttributeMap =
                        getSAMLAttributeMap(r.getSession());
                // TODO pahuang extract the key to system properties
                Optional<String> usernameFromSSO =
                        getValueFromSessionAttribute(samlAttributeMap, "uid");
                Optional<String> emailFromSSO = getValueFromSessionAttribute(samlAttributeMap, "email");
                Optional<String> surnameFromSSO = getValueFromSessionAttribute(samlAttributeMap, "sn");
                Optional<String> givenNameFromSSO = getValueFromSessionAttribute(samlAttributeMap, "givenName");

                if (acc.getRoles().contains("authenticated")) {
                    String principalName = acc.getPrincipal().getName();
                    log.info("SSO login: username: {}, uuid: {}",
                            usernameFromSSO, principalName);
                    String name = givenNameFromSSO.orElse("") + " " + surnameFromSSO.orElse("");
                    authenticationManager.ssoLogin(acc,
                            usernameFromSSO.orElse(principalName),
                            emailFromSSO.orElse(""), name);
                    performRedirection((HttpServletResponse) response);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private static Optional<String> getValueFromSessionAttribute(
            Optional<Map<String, List<String>>> samlAttributeMap, String key) {
        return samlAttributeMap.flatMap(m -> m.get(key).stream().findFirst());
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, List<String>>>
            getSAMLAttributeMap(HttpSession session) {
        Object attributeMap =
                session.getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP);
        return attributeMap != null && attributeMap instanceof Map
                ? Optional.of((Map<String, List<String>>) attributeMap)
                : Optional.empty();
    }

    @Override
    public void destroy() {
    }

    /**
     * Performs the redirection based on the results from the authentication
     * process.
     * This is logic that would normally be in faces-config.xml, but as this is
     * a servlet, it cannot take advantage of that.
     */
    private void performRedirection(HttpServletResponse resp) throws IOException {
        String authRedirectResult =
                authenticationManager.getAuthenticationRedirect();
        switch (authRedirectResult) {
            case "login":
                resp.sendRedirect(urlUtil.signInPage());
                break;

            case "edit":
                resp.sendRedirect(urlUtil.createUserPage());
                break;

            case "inactive":
                resp.sendRedirect(urlUtil.inactiveAccountPage());
                break;

            case "dashboard":
                resp.sendRedirect(urlUtil.dashboardUrl());
                break;
            case "redirect":
                // sso should not have any continue url. We just send to dashboard
                resp.sendRedirect(urlUtil.dashboardUrl());
                break;

            case "home":
                resp.sendRedirect(urlUtil.home());
                break;

            default:
                throw new RuntimeException(
                        "Unexpected authentication manager result: " +
                                authRedirectResult);
        }
    }
}
