package org.zanata.action;

import java.io.Serializable;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.zanata.ApplicationConfiguration;
import org.zanata.security.AuthenticationManager;
import org.zanata.security.UserRedirectBean;
import org.zanata.security.ZanataIdentity;
import org.zanata.util.UrlUtil;

/**
 * Action bean for openid.xhtml.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("openIdAction")
@ViewScoped
public class OpenIdAction implements Serializable {

    @Inject
    private ZanataIdentity identity;

    @Inject
    private UserRedirectBean userRedirect;

    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "CDI proxies are Serializable")
    private AuthenticationManager authenticationManager;

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private UrlUtil urlUtil;

    /**
     * Handle redirection after openId login.
     *
     * Cannot use faces-config.xml to handle due to <to-view-id> not supporting
     * EL expression.
     * <to-view-id>#{urlUtil.redirectToInternal(userRedirect.getUrl())}</to-view-id>
     */
    public void handleRedirect() {
        if(authenticationManager.isAuthenticated() && authenticationManager.isNewUser()) {
            urlUtil.redirectToInternal(urlUtil.createUserPage());
        } else if (!identity.isLoggedIn() && authenticationManager.isAuthenticatedAccountWaitingForActivation()) {
            urlUtil.redirectToInternal(urlUtil.inactiveAccountPage());
        } else if (!identity.isLoggedIn() && applicationConfiguration.isSingleOpenIdProvider()) {
            urlUtil.redirectToInternal(urlUtil.home());
        } else if (identity.isLoggedIn() && userRedirect.shouldRedirectToDashboard()) {
            urlUtil.redirectToInternal(urlUtil.dashboardUrl());
        } else if (!identity.isLoggedIn()) {
            urlUtil.redirectToInternal(urlUtil.signInPage());
        } else if (identity.isLoggedIn() && userRedirect.isRedirect()) {
            urlUtil.redirectToInternal(userRedirect.getUrl());
        } else if (identity.isLoggedIn()) {
            urlUtil.redirectToInternal(urlUtil.dashboardUrl());
        }
    }
}
