/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Account;

import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.io.Serializable;

/**
 * Represents user accounts in the system.
 * username: User name that identifies an account
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Path(AccountResource.SERVICE_PATH)
@ResourceLabel("User Account")
public interface AccountResource extends Serializable {
    public static final String SERVICE_PATH =
            "/accounts/u/{username:[a-z\\d_]{3,20}}";

    /**
     * Retrieves a user account.
     */
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML,
            MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON })
    @TypeHint(Account.class)
    @StatusCodes({
            @ResponseCode(code = 200,
                    condition = "response containing information for the user account"),
            @ResponseCode(code = 500,
                    condition = "If there is an unexpected error in the server while performing this operation")
    })
    public Response get();

    /**
     * Creates or updates a user account. If an account with the given user name
     * already exists, said account will be overwritten with the provided data.
     * Otherwise, a new account will be created.
     *
     * @param account
     *            The account information to create/update.
     */
    @PUT
    @Consumes({ MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML,
            MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON })
    @StatusCodes({
            @ResponseCode(code = 200,
                    condition = "An existing account was modified"),
            @ResponseCode(code = 201,
                    condition = "A new account was created"),
            @ResponseCode(code = 401,
                    condition = "The user does not have the proper permissions to perform this operation"),
            @ResponseCode(code = 500,
                    condition = "If there is an unexpected error in the server while performing this operation")
    })
    public Response put(Account account);

}
