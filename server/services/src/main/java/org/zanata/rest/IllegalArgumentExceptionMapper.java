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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.exception.ZanataServiceException;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;

@Provider
public class IllegalArgumentExceptionMapper implements
        ExceptionMapper<IllegalArgumentException> {
    private static Logger log = LoggerFactory.getLogger(
            IllegalArgumentExceptionMapper.class);

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        // see https://issues.jboss.org/browse/RESTEASY-411
        if (exception.getMessage().contains(
                "Failure parsing MediaType")) {
            return Response.status(UNSUPPORTED_MEDIA_TYPE)
                    .entity(exception.getMessage()).build();
        } else {
            log.error("IllegalArgumentException", exception);
            return Response.status(INTERNAL_SERVER_ERROR)
                    .entity("Error processing request").build();
        }
    }
}
