package org.zanata.events;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LocaleSelectedEvent {
    public static final String EVENT_NAME = "org.zanata.events.LocaleSelectedEvent";
    private final String locale;

    public LocaleSelectedEvent(String locale) {
        this.locale = locale;
    }
}
