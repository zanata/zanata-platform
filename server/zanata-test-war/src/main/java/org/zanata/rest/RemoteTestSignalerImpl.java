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
package org.zanata.rest;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.security.annotations.NoSecurityCheck;
import org.zanata.test.SessionTracker;

/**
 * Default implementation for the Remote Signaler interface.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Path("/remote/signal")
@NoSecurityCheck
public class RemoteTestSignalerImpl {
    private static final Logger log =
            LoggerFactory.getLogger(RemoteTestSignalerImpl.class);

    @Inject
    private SessionTracker sessionTracker;

    @POST
    @Path("/before")
    public void signalBeforeTest(@QueryParam("testClass") String testClass,
            @QueryParam("method") String testMethod) throws Exception {
        sessionTracker.invalidateAllSessions();
        log.info("Starting test {}:{}", testClass, testMethod);
    }

    @POST
    @Path("/after")
    public void signalAfterTest(@QueryParam("testClass") String testClass,
            @QueryParam("method") String testMethod) throws Exception {
        // Note: this will also trigger SessionChecker to check the
        // serializability of remaining sessions as they are destroyed:
        sessionTracker.invalidateAllSessions();
        log.info("Finished test {}:{}", testClass, testMethod);
    }
}
