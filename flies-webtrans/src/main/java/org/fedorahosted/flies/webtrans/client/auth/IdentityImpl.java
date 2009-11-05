package org.fedorahosted.flies.webtrans.client.auth;

import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;

import org.fedorahosted.flies.gwt.auth.Permission;
import org.fedorahosted.flies.gwt.auth.Role;
import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.gwt.rpc.AuthenticateAction;
import org.fedorahosted.flies.gwt.rpc.AuthenticateResult;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class IdentityImpl implements Identity {

	private String sessionId;
	
	private Set<Permission> permissions = new HashSet<Permission>();
	private Set<Role> roles = new HashSet<Role>();
	private Person person;
	
	private final DispatchAsync dispatcher;
	private final EventBus eventBus;
	
	@Inject
	public IdentityImpl(CachingDispatchAsync dispatcher, EventBus eventBus) {
		this.dispatcher = dispatcher;
		this.eventBus = eventBus;
		roles.add(Role.Anonymous);
		this.sessionId = Cookies.getCookie("JSESSIONID");
	}
	
	@Override
	public boolean isLoggedIn() {
		return sessionId != null;
	}

	@Override
	public boolean hasRole(Role role) {
		return roles.contains(role);
	}
	
	@Override
	public boolean hasPermission(Permission permission) {
		return permissions.contains(permission);
	}
	
	@Override
	public Person getPerson() {
		return person;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}
	
	@Override
	public void invalidate() {
		invalidateQuiet();
		eventBus.fireEvent( new UserLogoutEvent());
	}

	private void invalidateQuiet() {
		sessionId = null;
		permissions.clear();
		roles.clear();
		person = null;
		roles.add(Role.Anonymous);
		Cookies.removeCookie("JSESSIONID");
	}
	
	@Override
	public void login(String username, String password, final LoginResult callback) {
		
		dispatcher.execute(new AuthenticateAction(username, password), new AsyncCallback<AuthenticateResult>() {
			
			@Override
			public void onSuccess(AuthenticateResult result) {
				Cookies.setCookie("JSESSIONID", result.getSessionId().getValue());
				eventBus.fireEvent( new UserLoginEvent(result.getPerson()) );
				callback.onSuccess();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				invalidateQuiet();
				callback.onFailure();
			}
		});
	}
	
}
