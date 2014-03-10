package org.zanata.rest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO potential bean to be monitored
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("activeApiKeys")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Slf4j
public class ActiveApiKeys {
    private ThreadLocal<String> apiKeys = new ThreadLocal<String>();

    public void setApiKeyForCurrentThread(String apiKey) {
        apiKeys.set(apiKey);
    }

    public void removeApiKeyFromCurrentThread() {
        apiKeys.remove();
    }

    public String getApiKeyForCurrentThread() {
        return apiKeys.get();
    }

    public static ActiveApiKeys getInstance() {
        return (ActiveApiKeys) Component.getInstance(ActiveApiKeys.class);
    }
}
