/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Check whether the application server has defined obsolete JNDI entries. If
 * the entries exists, it's a sign that the sys admin has not follow the
 * migration guide properly. Will abort the deployment and give warning
 * message.
 *
 * TODO remove this once we have next release.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class ObsoleteJNDIChecker {

    void noObsoleteJNDIEntriesOrThrow() {
        try {
            Object zanataJNDI = new InitialContext()
                    .lookup("java:global/zanata");
            if (zanataJNDI != null) {
                throw new IllegalStateException(
                        "Zanata has replaced JNDI entries to system properties. See release note for how to migrate.");
            }
        } catch (NamingException e) {
            // it's good that we don't have this JNDI entry defined
        }
    }
}
