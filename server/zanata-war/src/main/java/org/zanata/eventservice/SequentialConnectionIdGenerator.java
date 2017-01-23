/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.eventservice;

import javax.servlet.http.HttpServletRequest;
import de.novanic.eventservice.service.connection.id.ConnectionIdGenerator;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class SequentialConnectionIdGenerator implements ConnectionIdGenerator {
    private static final Object $LOCK = new Object[0];

    private static long nextConnectionNum = 0;

    private static long generateConnectionNum() {
        synchronized (SequentialConnectionIdGenerator.$LOCK) {
            return nextConnectionNum++;
        }
    }

    @Override
    public String generateConnectionId(HttpServletRequest aRequest) {
        return String.valueOf(aRequest.getSession(true).getId() + "-"
                + generateConnectionNum());
    }

    @Override
    public String getConnectionId(HttpServletRequest aRequest) {
        return aRequest.getParameter("id");
    }
}
