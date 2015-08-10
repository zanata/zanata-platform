package org.zanata.events;

import org.zanata.model.HAccount;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class UserCreatedEvent {
    // TODO [CDI] remove constant after switching to CDI
    public static final String EVENT_NAME = "org.zanata.events.UserCreatedEvent";
    private final HAccount user;

    public UserCreatedEvent(HAccount user) {
        this.user = user;
    }

    public HAccount getUser() {
        return user;
    }
}
