/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.servlet

import java.io.File
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

/**
 * We don't want to build picketlink.xml into the war file, so we use
 * the system property picketlink.file to allow the system administrator to
 * pass in the location of picketlink.xml.
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
@WebListener
class PicketlinkFileHelper : ServletContextListener {

    override fun contextInitialized(event: ServletContextEvent) {
        val file = System.getProperty("picketlink.file")
        if (file != null) {
            if (!File(file).exists()) {
                throw RuntimeException("Can't read picketlink.file $file")
            }
            event.servletContext.setInitParameter("CONFIG_FILE", file)
        }
    }

    override fun contextDestroyed(event: ServletContextEvent) {
        // nothing to do
    }
}
