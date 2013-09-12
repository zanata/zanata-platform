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
package org.zanata.security;

import static org.jboss.seam.ScopeType.SESSION;

import java.util.List;

import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesManager;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.openid.OpenIdPrincipal;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.FetchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.security.openid.FedoraOpenIdProvider;
import org.zanata.security.openid.GenericOpenIdProvider;
import org.zanata.security.openid.GoogleOpenIdProvider;
import org.zanata.security.openid.MyOpenIdProvider;
import org.zanata.security.openid.OpenIdAuthCallback;
import org.zanata.security.openid.OpenIdAuthenticationResult;
import org.zanata.security.openid.OpenIdProvider;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.security.openid.YahooOpenIdProvider;


@Name("org.jboss.seam.security.zanataOpenId")
@Scope(SESSION)
@AutoCreate
/*
 * based on org.jboss.seam.security.openid.OpenId class
 */
public class ZanataOpenId implements OpenIdAuthCallback
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ZanataOpenId.class);

   private ZanataIdentity identity;
   private ApplicationConfiguration applicationConfiguration;

   @In
   private Credentials credentials;

   @In
   private UserRedirectBean userRedirect;

   @In
   private AccountDAO accountDAO;

   private String id;
   private OpenIdAuthenticationResult authResult;
   private OpenIdAuthCallback callback;
   private OpenIdProvider openIdProvider;

   private ConsumerManager manager;
   private DiscoveryInformation discovered;

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public OpenIdAuthenticationResult getAuthResult()
   {
      return authResult;
   }

   public void setCallback(OpenIdAuthCallback callback)
   {
      this.callback = callback;
   }

   @SuppressWarnings("rawtypes")
   protected String authRequest(String userSuppliedString, String returnToUrl)
   {
      try
      {
         // perform discovery on the user-supplied identifier
         List discoveries = manager.discover(userSuppliedString);

         // attempt to associate with the OpenID providerType
         // and retrieve one service endpoint for authentication
         discovered = manager.associate(discoveries);

         // // store the discovery information in the user's session
         // httpReq.getSession().setAttribute("openid-disc", discovered);

         // obtain a AuthRequest message to be sent to the OpenID providerType
         AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

         // Attribute Exchange example: fetching the 'email' attribute
         FetchRequest fetch = FetchRequest.createFetchRequest();
         openIdProvider.prepareRequest(fetch);

         // attach the extension to the authentication request
         authReq.addExtension(fetch);

         return authReq.getDestinationUrl(true);
      }
      catch (OpenIDException e)
      {
         LOGGER.warn("exception", e);
      }

      return null;
   }

   public void verify()
   {
      ExternalContext context = javax.faces.context.FacesContext.getCurrentInstance().getExternalContext();
      HttpServletRequest request = (HttpServletRequest) context.getRequest();

      authResult.setAuthenticatedId( verifyResponse(request) );
   }

   public boolean loginImmediately()
   {
      if (authResult.isAuthenticated())
      {
         Identity.instance().acceptExternallyAuthenticatedPrincipal((new OpenIdPrincipal(authResult.getAuthenticatedId())));
         return true;
      }

      return false;
   }

   public String verifyResponse(HttpServletRequest httpReq)
   {
      try
      {
         // extract the parameters from the authentication response
         // (which comes in as a HTTP request from the OpenID providerType)
         ParameterList response = new ParameterList(httpReq.getParameterMap());

         StringBuilder receivingURL = new StringBuilder(returnToUrl());
         String queryString = httpReq.getQueryString();
         if (queryString != null && queryString.length() > 0)
         {
            receivingURL.append("?").append(httpReq.getQueryString());
         }

         // verify the response; ConsumerManager needs to be the same
         // (static) instance used to place the authentication request
         VerificationResult verification = manager.verify(receivingURL.toString(), response, discovered);

         // The OpenId provider cancelled the authentication
         if( "cancel".equals( response.getParameterValue("openid.mode") ) )
         {
            // TODO This should be done at a higher level. i.e. instead of returning a string, return an
            // object that holds more information for the UI to render
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Authentication Request Cancelled");
         }

         // examine the verification result and extract the verified identifier
         Identifier verified = verification.getVerifiedId();
         if (verified != null)
         {
            authResult = new OpenIdAuthenticationResult();
            authResult.setAuthenticatedId( verified.getIdentifier() );
            authResult.setEmail( openIdProvider.getEmail(response) ); // Get the email address
         }

         // invoke the callbacks
         if( callback != null )
         {
            callback.afterOpenIdAuth(authResult);
            if( callback.getRedirectToUrl() != null )
            {
               userRedirect.setLocalUrl(callback.getRedirectToUrl());
            }
         }

         if( verified != null )
         {
            return verified.getIdentifier();
         }
      }
      catch (OpenIDException e)
      {
         LOGGER.warn("exception", e);
      }

      return null;
   }

   public void logout()
   {
      init();
   }

   @Create
   public void init()
   {
      manager = new ConsumerManager();
      discovered = null;
      id = null;
      authResult = new OpenIdAuthenticationResult();
      identity = (ZanataIdentity) Component.getInstance(ZanataIdentity.class, ScopeType.SESSION);
      applicationConfiguration = (ApplicationConfiguration) Component.getInstance(ApplicationConfiguration.class, ScopeType.APPLICATION);
   }
   
   private void loginImmediate()
   {
      if (loginImmediately() && Events.exists())
      {
         Events.instance().raiseEvent(Identity.EVENT_POST_AUTHENTICATE, identity);
         // Events.instance().raiseEvent(Identity.EVENT_LOGIN_SUCCESSFUL,
         // AuthenticationType.OPENID);
         Events.instance().raiseEvent(AuthenticationManager.EVENT_LOGIN_COMPLETED, AuthenticationType.OPENID);
      }
   }

   private void login(String username, OpenIdProviderType openIdProviderType, OpenIdAuthCallback callback)
   {
      try
      {
         this.setProvider(openIdProviderType);
         String var = openIdProvider.getOpenId(username);
         setId(var);
         setCallback(callback);
         LOGGER.info("openid: {}", getId());
         login();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void login(ZanataCredentials credentials)
   {
      this.login(credentials, this);
   }

   public void login(ZanataCredentials credentials, OpenIdAuthCallback callback)
   {
      this.login(credentials.getUsername(), credentials.getOpenIdProviderType(), callback);
   }

   private void login()
   {
      authResult = new OpenIdAuthenticationResult();
      String returnToUrl = returnToUrl();

      String url = authRequest(id, returnToUrl);

      if (url != null)
      {
         Redirect redirect = Redirect.instance();
         redirect.captureCurrentView();

         FacesManager.instance().redirectToExternalURL(url);
      }
   }

   public String returnToUrl()
   {
      return applicationConfiguration.getServerPath() + "/openid.seam";
   }

   /**
    * Default implementation for an authentication callback. This implementations simply authenticates
    * the user locally.
    */
   @Override
   public void afterOpenIdAuth(OpenIdAuthenticationResult result)
   {
      if( result.isAuthenticated() )
      {
         HAccount authenticatedAccount = accountDAO.getByCredentialsId( result.getAuthenticatedId() );

         identity.setPreAuthenticated(true);

         if( authenticatedAccount != null && authenticatedAccount.isEnabled() )
         {
            credentials.setUsername(authenticatedAccount.getUsername());
            Identity.instance().acceptExternallyAuthenticatedPrincipal((new OpenIdPrincipal(result.getAuthenticatedId())));
            this.loginImmediate();
         }
         // If the user hasn't been registered yet
         else if( authenticatedAccount == null )
         {
            credentials.setUsername(result.getAuthenticatedId()); // this is the full open id
         }
      }
   }

   /**
    * Default implementation for an authentication callback. This implementation does not provide a redirect url.
    */
   @Override
   public String getRedirectToUrl()
   {
      return null;
   }

   public void setProvider( OpenIdProviderType providerType )
   {
      if( providerType != null )
      {
         switch (providerType)
         {
            case Fedora:
               this.openIdProvider = new FedoraOpenIdProvider();
               break;

            case Google:
               this.openIdProvider = new GoogleOpenIdProvider();
               break;

            case MyOpenId:
               this.openIdProvider = new MyOpenIdProvider();
               break;

            case Yahoo:
               this.openIdProvider = new YahooOpenIdProvider();
               break;

            case Generic:
               this.openIdProvider = new GenericOpenIdProvider();
               break;

            default:
               this.openIdProvider = new GenericOpenIdProvider();
               break;
         }
      }
   }
}
