package org.zanata.rest.service;

import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.Session;
import org.jboss.resteasy.spi.NoLogWebApplicationException;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckRole;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.AccountRoleDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.rest.dto.Account;

@RequestScoped
@Named("accountService")
@Path(AccountResource.SERVICE_PATH)
@Transactional
@CheckRole("admin")
public class AccountService implements AccountResource {
    private static final Logger log =
            LoggerFactory.getLogger(AccountService.class);

    /**
     * User name that identifies an account.
     */
    @PathParam("username")
    String username;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @Context
    private HttpServletRequest request;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    @Context
    private UriInfo uri;
    @Inject
    private AccountDAO accountDAO;
    @Inject
    private AccountRoleDAO accountRoleDAO;
    @Inject
    private LocaleDAO localeDAO;
    @Inject
    private ZanataIdentity identity;
    @Inject
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
        getAccountDetails(hAccount, result);
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
        updateAccount(account, hAccount);
        // entity permission check
        identity.checkPermission(hAccount, operation);
        session.save(hAccount);
        session.flush();
        return response.build();
    }

    private void updateAccount(Account from, HAccount to) {
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
                log.debug("Invalid role \'{}\'", role);
                throw new NoLogWebApplicationException(Response
                        .status(Status.BAD_REQUEST)
                        .entity("Invalid role \'" + role + "\'").build());
            }
            to.getRoles().add(hAccountRole);
        }
        hPerson.getLanguageMemberships().clear();
        for (String language : from.getLanguages()) {
            HLocale hLocale = localeDAO.findByLocaleId(new LocaleId(language));
            if (hLocale == null)
                // generate error for missing language
                throw new NoLogWebApplicationException(Response
                        .status(Status.BAD_REQUEST)
                        .entity("Invalid language \'" + language + "\'").build());
            hPerson.getLanguageMemberships().add(hLocale);
        }
        to.setUsername(from.getUsername());
    }

    public static void getAccountDetails(HAccount from, Account to) {
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
