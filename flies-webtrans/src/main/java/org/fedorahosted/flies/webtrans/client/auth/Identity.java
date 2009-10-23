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

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class Identity {

	private static Identity instance;
	
	private String sessionId;
	
	private Set<Permission> permissions = new HashSet<Permission>();
	private Set<Role> roles = new HashSet<Role>();
	private Person person;
	
	private final DispatchAsync dispatcher;
	private final EventBus eventBus;
	
	@Inject
	public Identity(DispatchAsync dispatcher, EventBus eventBus) {
		this.dispatcher = dispatcher;
		this.eventBus = eventBus;
		instance = this;
		roles.add(Role.Anonymous);
	}
	
	public boolean isLoggedIn() {
		return sessionId != null;
	}
	
	public boolean hasRole(String role) {
		return roles.contains(role);
	}
	public boolean hasRole(Role role) {
		return roles.contains(role);
	}
	
	public boolean hasPermission(Permission permission) {
		return permissions.contains(permission);
	}
	
	public Person getPerson() {
		return person;
	}

	public String getSessionId() {
		return sessionId;
	}
	
	public static Identity instance() {
		return instance;
	}

	public void invalidate() {
		sessionId = null;
		permissions.clear();
		roles.clear();
		person = null;
		roles.add(Role.Anonymous);
		Cookies.removeCookie("JSESSIONID");
	}

	public void login(String username, String password, final LoginResult callback) {
		dispatcher.execute(new AuthenticateAction(username, password), new AsyncCallback<AuthenticateResult>() {
			
			@Override
			public void onSuccess(AuthenticateResult result) {
				Cookies.setCookie("JSESSIONID", result.getSessionId());
				eventBus.fireEvent( new UserLoginEvent() );
				callback.onSuccess();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure();
			}
		});
	}
	
}
