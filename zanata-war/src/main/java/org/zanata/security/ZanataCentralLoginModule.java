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
package org.zanata.security;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.jboss.seam.Component;
import org.zanata.config.JndiBackedConfig;


/**
 * This is a login module that works as a central dispatcher for all other configurable
 * login modules.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ZanataCentralLoginModule implements LoginModule
{

   private String internalAuthDomain;
   private String kerberosDomain;
   private String openIdDomain;
   private String jaasDomain;

   private Subject subject;
   private CallbackHandler callbackHandler;

   @Override
   public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options)
   {
      this.subject = subject;
      this.callbackHandler = callbackHandler;

      // Avoid using Seam components in Login Modules
      JndiBackedConfig jndiConfig = new JndiBackedConfig();

      internalAuthDomain = jndiConfig.getAuthPolicyName(AuthenticationType.INTERNAL.name().toLowerCase());
      kerberosDomain = jndiConfig.getAuthPolicyName(AuthenticationType.KERBEROS.name().toLowerCase());
      openIdDomain = jndiConfig.getAuthPolicyName(AuthenticationType.OPENID.name().toLowerCase());
      jaasDomain = jndiConfig.getAuthPolicyName(AuthenticationType.JAAS.name().toLowerCase());
   }

   @Override
   public boolean login() throws LoginException
   {
      AuthenticationTypeCallback authTypeCallback = new AuthenticationTypeCallback();

      // Get the authentication type
      try
      {
         callbackHandler.handle(new Callback[]{authTypeCallback});
      }
      catch( UnsupportedCallbackException ucex )
      {
         // This happens on kerberos authentication
         // NB: A custom callback handler could be configured on the app server to avoid this.
         authTypeCallback.setAuthType( AuthenticationType.KERBEROS );
      }
      catch (Exception e)
      {
         LoginException lex = new LoginException(e.getMessage());
         lex.initCause(e);
         throw lex;
      }
      AuthenticationType authType = authTypeCallback.getAuthType();

      String delegateName = null;
      switch (authType)
      {
         case INTERNAL:
            delegateName = internalAuthDomain;
            break;
         case KERBEROS:
            delegateName = kerberosDomain;
            break;
         case OPENID:
            delegateName = openIdDomain;
            break;
         case JAAS:
            delegateName = jaasDomain;
            break;
      }

      LoginContext delegate = new LoginContext(delegateName, subject, callbackHandler);
      delegate.login();
      return true;
   }

   @Override
   public boolean commit() throws LoginException
   {
      return true;
   }

   @Override
   public boolean abort() throws LoginException
   {
      return true;
   }

   @Override
   public boolean logout() throws LoginException
   {
      return true;
   }
}
