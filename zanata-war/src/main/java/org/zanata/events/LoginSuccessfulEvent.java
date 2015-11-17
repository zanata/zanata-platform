package org.zanata.events;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LoginSuccessfulEvent {
    private String name;

    public LoginSuccessfulEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
