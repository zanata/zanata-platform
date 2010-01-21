package org.fedorahosted.flies.webtrans.client.auth;

public interface LoginResult {
	void onFailure();
	void onSuccess();
}
