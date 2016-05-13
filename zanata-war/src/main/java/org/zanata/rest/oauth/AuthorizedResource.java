/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.rest.oauth;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.rest.dto.Account;
import org.zanata.rest.service.AccountService;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.oauth.SecurityTokens;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Path("/oauth/authorized")
@Slf4j
@Transactional
public class AuthorizedResource {

    @Inject
    private SecurityTokens securityTokens;

    @Context
    private HttpServletRequest request;

    @Inject
    private ProjectDAO projectDAO;

    @Inject
    private AccountDAO accountDAO;

    @Inject
    @Authenticated
    private HAccount authenticatedAccount;


    @Path("/myaccount")
    @Produces("application/json")
    @GET
    public Response accountDetail() {

        HAccount account = accountDAO.getByUsername(authenticatedAccount.getUsername());
        // TODO we don't need to return apiKey anymore once client switched to OAuth
        if (Strings.isNullOrEmpty(authenticatedAccount.getApiKey())) {
            accountDAO.createApiKey(account);
        }
        Account dto = new Account();
        AccountService.transfer(account, dto);
        return Response.ok(dto).build();
    }

}
