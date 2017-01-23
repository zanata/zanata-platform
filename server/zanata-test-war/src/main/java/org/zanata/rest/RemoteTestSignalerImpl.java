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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.zanata.security.annotations.NoSecurityCheck;

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
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RemoteTestSignalerImpl.class);

    @POST
    @Path("/before")
    public void signalBeforeTest(@QueryParam("testClass") String testClass,
            @QueryParam("method") String testMethod) throws Exception {
        log.info("Starting test {}:{}", testClass, testMethod);
    }

    @POST
    @Path("/after")
    public void signalAfterTest(@QueryParam("testClass") String testClass,
            @QueryParam("method") String testMethod) throws Exception {
        log.info("Finished test {}:{}", testClass, testMethod);
    }
}
