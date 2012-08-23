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

import java.util.List;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesManager;
import org.jboss.seam.faces.Redirect;
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
import org.zanata.security.openid.OpenIdProvider;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;


@Name("org.jboss.seam.security.fedoraOpenId")
@Scope(SESSION)
@Install(precedence = APPLICATION)
//@BypassInterceptors
/*
 * based on org.jboss.seam.security.openid.OpenId class
 */
public class FedoraOpenId
{
   private static final String FEDORA_HOST = ".id.fedoraproject.org/";
   private static final Logger LOGGER = LoggerFactory.getLogger(FedoraOpenId.class);

   private ZanataIdentity identity;
   private ApplicationConfiguration applicationConfiguration;

   @In
   private EntityManager entityManager;

   @In
   private Credentials credentials;

   private String id;
   private String validatedId;
   private String validatedEmail;

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

   public String getValidatedEmail()
   {
      return validatedEmail;
   }

   public void login()
   {
      validatedId = null;
      String returnToUrl = returnToUrl();

      String url = authRequest(id, returnToUrl);

      if (url != null)
      {
         Redirect redirect = Redirect.instance();
         redirect.captureCurrentView();

         FacesManager.instance().redirectToExternalURL(url);
      }
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
         /*fetch.addAttribute("email", "http://schema.openid.net/contact/email", // type
                                                                               // URI
               true); // required*/

         // Add the attributes requested by the provider
         OpenIdProvider provider = (OpenIdProvider)Component.getInstance("openIdProvider", ScopeType.SESSION);
         provider.prepareFetchRequest(fetch);

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

      validatedId = verifyResponse(request);
   }

   public boolean loginImmediately()
   {
      if (validatedId != null)
      {
         Identity.instance().acceptExternallyAuthenticatedPrincipal((new OpenIdPrincipal(validatedId)));
         return true;
      }

      return false;
   }

   public boolean isValid()
   {
      return validatedId != null;
   }


   public String getValidatedId()
   {
      return validatedId;
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

         OpenIdProvider provider = (OpenIdProvider)Component.getInstance("openIdProvider", ScopeType.SESSION);

         // examine the verification result and extract the verified identifier
         Identifier verified = verification.getVerifiedId();
         if (verified != null)
         {
            // Set the credentials' user name to authenticated Zanata user
            credentials.setUsername(this.getZanataUsername(verified.getIdentifier()));
            validatedEmail = provider.extractEmailAddress( response );
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
      try
      {
         manager = new ConsumerManager();
         discovered = null;
         id = null;
         validatedId = null;
      }
      catch (ConsumerException e)
      {
         throw new RuntimeException(e);
      }
      identity = (ZanataIdentity) Component.getInstance(ZanataIdentity.class, ScopeType.SESSION);
      applicationConfiguration = (ApplicationConfiguration) Component.getInstance(ApplicationConfiguration.class, ScopeType.APPLICATION);
   }
   
   public void loginImmediate()
   {
      if (loginImmediately())
      {
         if (Events.exists())
         {
            Events.instance().raiseEvent(Identity.EVENT_POST_AUTHENTICATE, identity);
         }
         if (Events.exists())
         {
            Events.instance().raiseEvent(Identity.EVENT_LOGIN_SUCCESSFUL);
         }
      }
   }

   public void login(String username)
   {
      try
      {
         OpenIdProvider openIdProvider = (OpenIdProvider)Component.getInstance("openIdProvider", ScopeType.SESSION);

         String var = openIdProvider.getOpenId(username);
         setId(var);
         LOGGER.info("openid: {}", getId());
         login();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public String returnToUrl()
   {
      return applicationConfiguration.getServerPath() + "/openid.seam";
   }

   /**
    * Looks up a zanata user name based on the open id provided.
    * If none is found, returns null.
    */
   private String getZanataUsername( String openId )
   {
      List results =
            entityManager.createQuery("select c.account.username from HCredentials c where c.user = :openId")
                              .setParameter("openId", openId)
                              .getResultList();

      return results.size() > 0 ? (String)results.get(0) : null;
   }

}
