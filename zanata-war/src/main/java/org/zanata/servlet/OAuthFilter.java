package org.zanata.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.oauth.OAuthUtil;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.oauth.SecurityTokens;
import org.zanata.util.ServiceLocator;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author <a href='mailto:pahuang@redhat.com>pahuang</a>
 */
@WebFilter(filterName = "OAuthFilter", urlPatterns = "/authorize/*")
public class OAuthFilter implements Filter {
    private static final Logger log =
            LoggerFactory.getLogger(OAuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse =
                (HttpServletResponse) response;
        Optional<String> oauthRedirect = OAuthUtil.getOAuthRedirectURI(httpServletRequest);
        if (!oauthRedirect.isPresent()) {
            // an OAuth request without an redirect uri means it's not coming
            // from a third party app (possibly coming from a bot or
            // the third party app is not doing a proper request).
            // we will just present an error page
            throw new IllegalArgumentException("Accessing authorization page without a redirect URI");
        }

        // injection to fields doesn't seem to work
        ZanataIdentity identity = ZanataIdentity.instance();
        SecurityTokens securityTokens = ServiceLocator.instance().getInstance(
                        SecurityTokens.class);

        Optional<String> clientId = OAuthUtil.getOAuthClientId(httpServletRequest);

        if (!clientId.isPresent()) {
            throw new IllegalArgumentException("Accessing authorization page without a client id");
        }

        String authorizationCode = null;
        if (identity.isLoggedIn()) {
            Optional<String> code = securityTokens
                    .findAuthorizationCode(identity.getAccountUsername(),
                            clientId.get());
            if (code.isPresent()) {
                authorizationCode = code.get();
            }
        }

        if (authorizationCode != null) {
            String redirectLocationUrl = securityTokens
                    .getRedirectLocationUrl(httpServletRequest,
                            oauthRedirect.get(), authorizationCode);
            log.debug(
                    "authorization code already issued for this session. Username {}, client id: {}",
                    identity.getAccountUsername(), clientId);
            httpServletResponse.sendRedirect(redirectLocationUrl);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
