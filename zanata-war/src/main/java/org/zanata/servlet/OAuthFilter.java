package org.zanata.servlet;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.oauth.SecurityTokens;

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

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

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
        String oauthRedirect = request.getParameter(OAuth.OAUTH_REDIRECT_URI);
        // injection to fields doesn't seem to work
        if (Strings.isNullOrEmpty(oauthRedirect)) {
            // TODO should we throw error here?
            chain.doFilter(request, response);
            return;
        }

        ZanataIdentity identity =
                BeanProvider.getContextualReference(ZanataIdentity.class);
        SecurityTokens securityTokens =
                BeanProvider.getContextualReference(
                        SecurityTokens.class);

        String clientId = request.getParameter(OAuth.OAUTH_CLIENT_ID);

        String authorizationCode = null;
        if (identity.isLoggedIn()) {
            Optional<String> code = securityTokens
                    .tryGetByUsername(identity.getAccountUsername(),
                            clientId);
            if (code.isPresent()) {
                authorizationCode = code.get();
            }
        }

        if (authorizationCode != null) {
            try {
                OAuthResponse resp = OAuthASResponse
                        .authorizationResponse(httpServletRequest,
                                HttpServletResponse.SC_FOUND)
                        .setCode(authorizationCode)
                        .location(oauthRedirect)
                        .buildQueryMessage();
                log.info(
                        "authorization code already issued for this session. Username {}, client id: {}",
                        identity.getAccountUsername(), clientId);
                httpServletResponse.sendRedirect(resp.getLocationUri());
                return;
            } catch (OAuthSystemException e) {
                throw Throwables.propagate(e);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
