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

import java.io.Serializable;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.zanata.util.DefaultLocale;

/**
 * Factory bean to return an instance of Messages, based on a parameter, or for
 * the server default locale.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("messagesFactory")
@javax.enterprise.context.ApplicationScoped
public class MessagesFactory implements Serializable {

    private static final long serialVersionUID = 4503539056097043809L;

    /**
     * Returns an instance of Messages for the specified locale.
     */
    public Messages getMessages(Locale locale) {
        return new Messages(locale);
    }

    /**
     * Returns an instance of Messages for the server's default locale.
     * Also serves as a CDI Producer for @DefaultLocale Messages.
     */
    @Produces
    @DefaultLocale
    @ApplicationScoped
    public Messages getDefaultLocaleMessages() {
        return getMessages(Locale.getDefault());
    }
}
