package org.fedorahosted.flies.security;

import static org.jboss.seam.ScopeType.APPLICATION;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.management.IdentityManagementException;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.jboss.seam.util.AnnotatedBeanProperty;


@Name("org.jboss.seam.security.identityStore")
@Install(precedence = Install.DEPLOYMENT, value = true)
@Scope(APPLICATION)
@BypassInterceptors
public class FliesJpaIdentityStore extends JpaIdentityStore {

	private AnnotatedBeanProperty<UserApiKey> userApiKeyProperty;

	private static final LogProvider log = Logging
			.getLogProvider(FliesJpaIdentityStore.class);

	@Create
	public void init() {
		super.init();
		initProperties();
	}

	private void initProperties() {
		userApiKeyProperty = new AnnotatedBeanProperty(getUserClass(),
				UserApiKey.class);
		if (!userApiKeyProperty.isSet()) {
	         throw new IdentityManagementException("Invalid userClass " + getUserClass().getName() + 
	         " - required annotation @UserApiKey not found on any Field or Method.");         
		}
	}

	@Override
	public boolean authenticate(String username, String password) {
		FliesIdentity identity = FliesIdentity.instance();
		if (!identity.isApiRequest()) {
			return super.authenticate(username, password);
		}

		Object user = lookupUser(username);
		if (user == null || !isUserEnabled(username)) {
			return false;
		}

		if( ! userApiKeyProperty.isSet() ) {
			return false;
		}
		
		String userApiKey = (String) userApiKeyProperty.getValue(user);

		if( userApiKey == null) {
			return false;
		}
		
		boolean success = password.equals(userApiKey);

		if (success && Events.exists()) {
			if (Contexts.isEventContextActive()) {
				Contexts.getEventContext().set(AUTHENTICATED_USER, user);
			}

			Events.instance().raiseEvent(EVENT_USER_AUTHENTICATED, user);
		}

		return success;

	}
}
