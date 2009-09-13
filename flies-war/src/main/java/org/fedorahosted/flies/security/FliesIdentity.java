package org.fedorahosted.flies.security;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.fedorahosted.flies.core.dao.AccountDAO;
import org.fedorahosted.flies.core.model.HAccount;
import org.jboss.resteasy.plugins.server.embedded.SimplePrincipal;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;

@Name("org.jboss.seam.security.identity")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Startup
public class FliesIdentity extends Identity {

	private static final LogProvider log = Logging
			.getLogProvider(FliesIdentity.class);

	private String apiKey;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		getCredentials().setPassword(apiKey);
	}

	public boolean isApiRequest() {
		return apiKey != null;
	}

	public static FliesIdentity instance() {
		if (!Contexts.isSessionContextActive()) {
			throw new IllegalStateException("No active session context");
		}

		FliesIdentity instance = (FliesIdentity) Component.getInstance(
				FliesIdentity.class, ScopeType.SESSION);

		if (instance == null) {
			throw new IllegalStateException("No Identity could be created");
		}

		return instance;
	}

}
