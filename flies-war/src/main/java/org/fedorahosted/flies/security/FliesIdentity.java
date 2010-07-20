package org.fedorahosted.flies.security;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;

@Name("org.jboss.seam.security.identity")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
@Startup
public class FliesIdentity extends Identity
{

   public static final String USER_LOGOUT_EVENT = "user.logout";
   public static final String USER_ENTER_WORKSPACE = "user.enter";
   private String username;

   private static final LogProvider log = Logging.getLogProvider(FliesIdentity.class);

   private String apiKey;

   public String getApiKey()
   {
      return apiKey;
   }

   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
      getCredentials().setPassword(apiKey);
   }

   public boolean isApiRequest()
   {
      return apiKey != null;
   }

   public static FliesIdentity instance()
   {
      if (!Contexts.isSessionContextActive())
      {
         throw new IllegalStateException("No active session context");
      }

      FliesIdentity instance = (FliesIdentity) Component.getInstance(FliesIdentity.class, ScopeType.SESSION);

      if (instance == null)
      {
         throw new IllegalStateException("No Identity could be created");
      }

      return instance;
   }

   public void checkLoggedIn()
   {
      if (!isLoggedIn())
         throw new NotLoggedInException();
   }

   public void logout()
   {
      if (Events.exists())
         Events.instance().raiseEvent(USER_LOGOUT_EVENT, getPrincipal().getName());
      super.logout();
   }

   @Override
   public boolean hasPermission(Object target, String action)
   {
      if (log.isDebugEnabled())
         log.debug("ENTER hasPermission(" + target + "," + action + ")");
      boolean result = super.hasPermission(target, action);
      if (log.isDebugEnabled())
         log.debug("EXIT hasPermission(): " + result);
      return result;
   }

   @Override
   public boolean hasPermission(String name, String action, Object... arg)
   {
      if (log.isDebugEnabled())
         log.debug("ENTER hasPermission(" + name + "," + action + "," + arg + ")");
      boolean result = super.hasPermission(name, action, arg);
      if (log.isDebugEnabled())
         log.debug("EXIT hasPermission(): " + result);
      return result;
   }

}
