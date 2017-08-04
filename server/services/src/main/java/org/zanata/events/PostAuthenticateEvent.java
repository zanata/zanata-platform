package org.zanata.events;

import org.zanata.model.HAccount;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PostAuthenticateEvent {
    private final HAccount authenticatedAccount;

    public PostAuthenticateEvent(HAccount authenticatedAccount) {
        this.authenticatedAccount = authenticatedAccount;
    }

    public HAccount getAuthenticatedAccount() {
        return authenticatedAccount;
    }
}
