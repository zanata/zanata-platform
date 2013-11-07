/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.seam.mail;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.mail.MailSession;
import org.zanata.ApplicationConfiguration;

/**
 * Overrides Seam's default MailSession component to dynamically define
 * properties like host, port, etc.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("org.jboss.seam.mail.mailSession")
@Install(value = true, precedence = Install.APPLICATION,
        classDependencies = "javax.mail.Session")
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
public class ZanataMailSession extends MailSession {
    public ZanataMailSession() {
        this(null);
    }

    public ZanataMailSession(String transport) {
        super(transport);
        setup();
    }

    protected void setup() {
        ApplicationConfiguration appConfig =
                (ApplicationConfiguration) Component
                        .getInstance(ApplicationConfiguration.class);

        // Override default properties
        setHost(appConfig.getEmailServerHost());
        setPort(appConfig.getEmailServerPort());
        setUsername(appConfig.getEmailServerUsername());
        setPassword(appConfig.getEmailServerPassword());
        setTls(appConfig.useEmailServerTls());
        setSsl(appConfig.useEmailServerSsl());
    }
}
