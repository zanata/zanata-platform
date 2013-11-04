package org.zanata.rest.service;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.Session;
import org.jboss.resteasy.spi.NoLogWebApplicationException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.security.Identity;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.rest.dto.Account;

@Name("accountService")
@Path(AccountResource.SERVICE_PATH)
@Slf4j
@Transactional
public class AccountService implements AccountResource {
    /** User name that identifies an account. */
    @PathParam("username")
    String username;

    @Context
    private HttpServletRequest request;

    @Context
    private UriInfo uri;

    @In
    private AccountDAO accountDAO;

    @In
    private AccountRoleDAO accountRoleDAO;

    @In
    private LocaleDAO localeDAO;

    @In
    private Identity identity;

    @In
    private Session session;

    @Override
    public Response get() {
        log.debug("HTTP GET {}", request.getRequestURL());
        HAccount hAccount = accountDAO.getByUsername(username);
        if (hAccount == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Username not found").build();
        }
        Account result = new Account();
        transfer(hAccount, result);

        log.debug("HTTP GET result :\n" + result);
        return Response.ok(result).build();
    }

    @Override
    public Response put(Account account) {
        log.debug("HTTP PUT {} : \n{}", request.getRequestURL(), account);

        // RestUtils.validateEntity(account);
        HAccount hAccount = accountDAO.getByUsername(username);
        ResponseBuilder response;
        String operation;

        if (hAccount == null) {
            // creating
            operation = "insert";
            response = Response.created(uri.getAbsolutePath());

            hAccount = new HAccount();
            HPerson person = new HPerson();
            person.setAccount(hAccount);
            hAccount.setPerson(person);
        } else {
            // updating
            operation = "update";
            response = Response.ok();
        }

        transfer(account, hAccount);
        // entity permission check
        identity.checkPermission(hAccount, operation);
        session.save(hAccount);
        session.flush();

        return response.build();
    }

    private void transfer(Account from, HAccount to) {
        to.setApiKey(from.getApiKey());
        to.setEnabled(from.isEnabled());
        to.setPasswordHash(from.getPasswordHash());

        HPerson hPerson = to.getPerson();
        hPerson.setEmail(from.getEmail());
        hPerson.setName(from.getName());

        to.getRoles().clear();
        for (String role : from.getRoles()) {
            HAccountRole hAccountRole = accountRoleDAO.findByName(role);
            if (hAccountRole == null) {
                // generate error for missing role
                log.debug("Invalid role '{}'", role);
                throw new NoLogWebApplicationException(Response
                        .status(Status.BAD_REQUEST)
                        .entity("Invalid role '" + role + "'").build());
            }
            to.getRoles().add(hAccountRole);
        }

        hPerson.getLanguageMemberships().clear();
        for (String tribe : from.getTribes()) {
            HLocale hTribe = localeDAO.findByLocaleId(new LocaleId(tribe));
            if (hTribe == null)
                // generate error for missing tribe
                throw new NoLogWebApplicationException(Response
                        .status(Status.BAD_REQUEST)
                        .entity("Invalid tribe '" + tribe + "'").build());
            hPerson.getLanguageMemberships().add(hTribe);
        }

        to.setUsername(from.getUsername());
    }

    private void transfer(HAccount from, Account to) {
        to.setApiKey(from.getApiKey());
        to.setEnabled(from.isEnabled());
        to.setPasswordHash(from.getPasswordHash());

        HPerson hPerson = from.getPerson();
        to.setEmail(hPerson.getEmail());
        to.setName(hPerson.getName());

        Set<String> roles = new HashSet<String>();

        for (HAccountRole role : from.getRoles()) {
            roles.add(role.getName());
        }

        to.setRoles(roles);
        to.setUsername(from.getUsername());
    }
}
