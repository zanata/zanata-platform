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

import org.zanata.util.ZanataBasicConfig;

import static org.zanata.util.ZanataBasicConfig.KEY_AUTH_POLICY;

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

      // TODO Use JNDI variables to get this information
      ZanataBasicConfig basicConfig = ZanataBasicConfig.getInstance();

      internalAuthDomain = basicConfig.getProperty(KEY_AUTH_POLICY + "." + AuthenticationType.INTERNAL.name().toLowerCase());
      kerberosDomain = basicConfig.getProperty(KEY_AUTH_POLICY + "." + AuthenticationType.KERBEROS.name().toLowerCase());
      openIdDomain = basicConfig.getProperty(KEY_AUTH_POLICY + "." + AuthenticationType.OPENID.name().toLowerCase());
      jaasDomain = basicConfig.getProperty(KEY_AUTH_POLICY + "." + AuthenticationType.JAAS.name().toLowerCase());
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
}
