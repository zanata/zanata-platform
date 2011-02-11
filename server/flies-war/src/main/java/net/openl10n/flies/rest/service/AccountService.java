package net.openl10n.flies.rest.service;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.AccountDAO;
import net.openl10n.flies.dao.AccountRoleDAO;
import net.openl10n.flies.dao.LocaleDAO;
import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HAccountRole;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.rest.MediaTypes;
import net.openl10n.flies.rest.dto.Account;

import org.hibernate.Session;
import org.jboss.resteasy.spi.NoLogWebApplicationException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;

@Name("accountService")
@Path("/accounts/u/{username:[a-z\\d_]{3,20}}")
@Transactional
public class AccountService
{

   @PathParam("username")
   String username;

   @Context
   private HttpServletRequest request;

   @Context
   private UriInfo uri;

   Log log = Logging.getLog(AccountService.class);

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

   public AccountService()
   {
   }

   public AccountService(AccountDAO accountDAO)
   {
      this.accountDAO = accountDAO;
   }

   @GET
   @Produces(
   {MediaTypes.APPLICATION_FLIES_ACCOUNT_XML, MediaTypes.APPLICATION_FLIES_ACCOUNT_JSON})
   public Response get()
   {
      log.debug("HTTP GET {0}", request.getRequestURL());
      HAccount hAccount = accountDAO.getByUsername(username);
      if (hAccount == null)
      {
         return Response.status(Status.NOT_FOUND).entity("Username not found").build();
      }
      Account result = new Account();
      transfer(hAccount, result);

      log.debug("HTTP GET result :\n" + result);
      return Response.ok(result).build();
   }

   @PUT
   @Consumes(
   {MediaTypes.APPLICATION_FLIES_ACCOUNT_XML, MediaTypes.APPLICATION_FLIES_ACCOUNT_JSON})
   public Response put(Account account)
   {
      log.debug("HTTP PUT {0} : \n{1}", request.getRequestURL(), account);

      RestUtils.validateEntity(account);
      HAccount hAccount = accountDAO.getByUsername(username);
      ResponseBuilder response;
      String operation;

      if (hAccount == null)
      {
         // creating
         operation = "insert";
         response = Response.created(uri.getAbsolutePath());

         hAccount = new HAccount();
         HPerson person = new HPerson();
         person.setAccount(hAccount);
         hAccount.setPerson(person);
      }
      else
      {
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

   private void transfer(Account from, HAccount to)
   {
      to.setApiKey(from.getApiKey());
      to.setEnabled(from.isEnabled());
      to.setPasswordHash(from.getPasswordHash());

      HPerson hPerson = to.getPerson();
      hPerson.setEmail(from.getEmail());
      hPerson.setName(from.getName());

      to.getRoles().clear();
      for (String role : from.getRoles())
      {
         HAccountRole hAccountRole = accountRoleDAO.findByName(role);
         if (hAccountRole == null)
         {
            // generate error for missing role
            log.debug("Invalid role '{0}'", role);
            throw new NoLogWebApplicationException(Response.status(Status.BAD_REQUEST).entity("Invalid role '"+role+"'").build());
         }
         to.getRoles().add(hAccountRole);
      }

      hPerson.getTribeMemberships().clear();
      for (String tribe : from.getTribes())
      {
         HLocale hTribe = localeDAO.findByLocaleId(new LocaleId(tribe));
         if (hTribe == null)
            // generate error for missing tribe
            throw new NoLogWebApplicationException(Response.status(Status.BAD_REQUEST).entity(
                  "Invalid tribe '" + tribe + "'").build());
         hPerson.getTribeMemberships().add(hTribe);
      }

      to.setUsername(from.getUsername());
   }

   private void transfer(HAccount from, Account to)
   {
      to.setApiKey(from.getApiKey());
      to.setEnabled(from.isEnabled());
      to.setPasswordHash(from.getPasswordHash());

      HPerson hPerson = from.getPerson();
      to.setEmail(hPerson.getEmail());
      to.setName(hPerson.getName());

      Set<String> roles = new HashSet<String>();

      for (HAccountRole role : from.getRoles())
      {
         roles.add(role.getName());
      }

      to.setRoles(roles);
      to.setUsername(from.getUsername());
   }
}
