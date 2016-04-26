package org.zanata.events;

import java.util.Locale;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class LocaleSelectedEvent {
    private final Locale locale;

    public LocaleSelectedEvent(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }
}
