/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.i18n;

import org.zanata.util.DefaultLocale;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.util.Locale;

/**
 * Factory bean to return an instance of Messages, based on a parameter, or for
 * the server default locale.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("messagesFactory")
@javax.enterprise.context.ApplicationScoped
public class MessagesFactory {

    /**
     * Returns an instance of Messages for the server's default locale.
     */
    private final Messages defaultLocaleMessages =
            getMessages(Locale.getDefault());

    /**
     * Returns an instance of Messages for the specified locale.
     */
    public Messages getMessages(Locale locale) {
        return new Messages(locale);
    }

    @Produces
    @DefaultLocale
    @ApplicationScoped
    public Messages getApplicationMessages() {
        return defaultLocaleMessages;
    }

    /**
     * Returns an instance of Messages for the server's default locale.
     */
    public Messages getDefaultLocaleMessages() {
        return this.defaultLocaleMessages;
    }
}
