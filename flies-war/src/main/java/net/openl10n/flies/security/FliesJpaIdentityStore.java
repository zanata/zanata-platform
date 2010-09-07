package net.openl10n.flies.security;

import static org.jboss.seam.ScopeType.APPLICATION;

import javax.persistence.EntityManager;

import net.openl10n.flies.model.HAccount;
import net.openl10n.flies.model.HPerson;

import org.jboss.infrastructure.nukes.INukesAuthenticator;
import org.jboss.infrastructure.nukes.IUserInfo;
import org.jboss.infrastructure.nukes.InvalidLoginException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.management.IdentityManagementException;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.jboss.seam.util.AnnotatedBeanProperty;

@Name("org.jboss.seam.security.identityStore")
@Install(precedence = Install.DEPLOYMENT, value = true)
@Scope(APPLICATION)
@BypassInterceptors
public class FliesJpaIdentityStore extends JpaIdentityStore
{

   private static final Log log = Logging.getLog(FliesJpaIdentityStore.class);

   private AnnotatedBeanProperty<UserApiKey> userApiKeyProperty;

   public static final String EVENT_NEW_USER_LOGGED_IN = "net.openl10n.flies.auth.new-user-logged-in";

   @Create
   public void init()
   {
      super.init();
      initProperties();
   }

   private void initProperties()
   {
      userApiKeyProperty = new AnnotatedBeanProperty(getUserClass(), UserApiKey.class);
      if (!userApiKeyProperty.isSet())
      {
         throw new IdentityManagementException("Invalid userClass " + getUserClass().getName() + " - required annotation @UserApiKey not found on any Field or Method.");
      }
   }

   public boolean apiKeyAuthenticate(String username, String apiKey)
   {
      Object user = lookupUser(username);
      if (user == null || !isUserEnabled(username))
      {
         return false;
      }

      if (!userApiKeyProperty.isSet())
      {
         return false;
      }

      String userApiKey = (String) userApiKeyProperty.getValue(user);

      if (userApiKey == null)
      {
         return false;
      }

      boolean success = apiKey.equals(userApiKey);

      if (success && Events.exists())
      {
         if (Contexts.isEventContextActive())
         {
            Contexts.getEventContext().set(AUTHENTICATED_USER, user);
         }

         Events.instance().raiseEvent(EVENT_USER_AUTHENTICATED, user);
      }

      return success;
   }

   public boolean isNukesActive()
   {
      return Component.getInstance("nukesSecurityService", ScopeType.STATELESS, true) != null;
   }

   private boolean nukesAuthenticate(String username, String password)
   {
      INukesAuthenticator authenticator = (INukesAuthenticator) Component.getInstance("nukesSecurityService", ScopeType.STATELESS, true);
      try
      {
         IUserInfo userInfo = authenticator.authenticate(username, password);
         Object user = lookupUser(username);
         if (user == null)
         {
            user = createUser(userInfo);
            Events.instance().raiseEvent(EVENT_NEW_USER_LOGGED_IN, userInfo);
         }
         else if (!isUserEnabled(username))
         {
            log.info("User {0} attemted to log in to disabled account.", username);
            return false;
         }
         if (Events.exists())
         {
            if (Contexts.isEventContextActive())
            {
               Contexts.getEventContext().set(AUTHENTICATED_USER, user);
            }

            Events.instance().raiseEvent(EVENT_USER_AUTHENTICATED, user);
         }
         return true;
      }
      catch (InvalidLoginException e)
      {
         log.info("User {0} attemted to log in with incorrect password.", username);
         return false;
      }
   }

   @Override
   public boolean authenticate(String username, String password)
   {
      FliesIdentity identity = FliesIdentity.instance();
      if (identity.isApiRequest())
      {
         return apiKeyAuthenticate(username, password);
      }
      else if (isNukesActive())
      {
         return nukesAuthenticate(username, password);
      }
      else
      {
         return super.authenticate(username, password);
      }
   }

   private HAccount createUser(IUserInfo userInfo)
   {
      EntityManager em = (EntityManager) getEntityManager().getValue();
      HAccount account = new HAccount();
      account.setUsername(userInfo.getUsername());
      account.setEnabled(true);
      HPerson person = new HPerson();
      person.setName(userInfo.getDisplayName());
      person.setEmail(userInfo.getEmail());
      person.setAccount(account);
      account.setPerson(person);
      em.persist(account);
      em.refresh(account);
      return account;
   }
}
