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

import java.io.IOException;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.jboss.seam.Component;
import org.zanata.ApplicationConfiguration;

/**
 * This is a login module that works as a central dispatcher for all other configurable
 * login modules.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ZanataCentralLoginModule implements LoginModule
{
   // Module options
   private static final String INTERNAL_AUTH_DOMAIN = "internalAuthDomain";
   private static final String KERBEROS_DOMAIN = "kerberosDomain";
   private static final String OPENID_DOMAIN = "openIdDomain";
   private static final String JAAS_DOMAIN = "jaasDomain";

   private String internalAuthDomain;
   private String kerberosDomain;
   private String openIdDomain;
   private String jaasDomain;

   private Subject subject;
   private CallbackHandler callbackHandler;
   private Map<String, ?> options;

   private LoginContext delegate;

   @Override
   public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options)
   {
      this.subject = subject;
      this.callbackHandler = callbackHandler;
      this.options = options;

      // TODO USe JNDI variables to get this information
      ApplicationConfiguration appConfig = (ApplicationConfiguration)Component.getInstance(ApplicationConfiguration.class);

      internalAuthDomain = appConfig.getLoginModuleName(AuthenticationType.INTERNAL);
      kerberosDomain = appConfig.getLoginModuleName(AuthenticationType.KERBEROS);
      openIdDomain = appConfig.getLoginModuleName(AuthenticationType.OPENID);
      jaasDomain = appConfig.getLoginModuleName(AuthenticationType.JAAS);
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
      catch (Exception e)
      {
         LoginException lex = new LoginException(e.getMessage());
         lex.initCause(e);
         throw lex;
      }
      AuthenticationType authType = authTypeCallback.getAuthType();
      if( !isAuthTypeValid(authType) )
      {
         throw new LoginException("Invalid Authentication type: " + authType + ". Please check your server configuration");
      }

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

      delegate = new LoginContext(delegateName, subject, callbackHandler);
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

   private boolean isAuthTypeValid(AuthenticationType authType)
   {
      ApplicationConfiguration appConfig = (ApplicationConfiguration)Component.getInstance(ApplicationConfiguration.class);

      if( appConfig.isInternalAuth() && authType == AuthenticationType.INTERNAL )
      {
         return true;
      }
      else if ( appConfig.isKerberosAuth() && authType == AuthenticationType.KERBEROS )
      {
         return true;
      }
      else if( appConfig.isOpenIdAuth() && authType == AuthenticationType.OPENID )
      {
         return true;
      }
      else if( appConfig.isJaasAuth() && authType == AuthenticationType.JAAS )
      {
         return true;
      }

      return false;
   }
}
