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
package net.openl10n.flies.security;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.APPLICATION;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.openl10n.flies.ApplicationConfiguration;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.openid.OpenIdPrincipal;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.message.ParameterList;
import java.util.List;

import javax.faces.context.ExternalContext;


import org.jboss.seam.faces.FacesManager;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.security.Identity;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ax.FetchRequest;


@Name("org.jboss.seam.security.fedoraOpenId")
@Scope(SESSION)
@Install(precedence = APPLICATION)
@BypassInterceptors
/*
 * based on org.jboss.seam.security.openid.OpenId class
 */
public class FedoraOpenId
{
   private static final long serialVersionUID = 1L;
   private static String FEDORA_HOST = ".id.fedoraproject.org/";
   private static final LogProvider log = Logging.getLogProvider(FedoraOpenId.class);
   private FliesIdentity identity;
   private FliesJpaIdentityStore identityStore;
   private ApplicationConfiguration applicationConfiguration;
   private FliesExternalLoginBean fliesExternalLoginBean;

   private String id;
   private String validatedId;

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


   public void login() throws IOException
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
   protected String authRequest(String userSuppliedString, String returnToUrl) throws IOException
   {
      try
      {
         // perform discovery on the user-supplied identifier
         List discoveries = manager.discover(userSuppliedString);

         // attempt to associate with the OpenID provider
         // and retrieve one service endpoint for authentication
         discovered = manager.associate(discoveries);

         // // store the discovery information in the user's session
         // httpReq.getSession().setAttribute("openid-disc", discovered);

         // obtain a AuthRequest message to be sent to the OpenID provider
         AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

         // Attribute Exchange example: fetching the 'email' attribute
         FetchRequest fetch = FetchRequest.createFetchRequest();
         fetch.addAttribute("email", "http://schema.openid.net/contact/email", // type
                                                                               // URI
               true); // required

         // attach the extension to the authentication request
         authReq.addExtension(fetch);

         return authReq.getDestinationUrl(true);
      }
      catch (OpenIDException e)
      {
         log.warn(e.getMessage());
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
         // (which comes in as a HTTP request from the OpenID provider)
         ParameterList response = new ParameterList(httpReq.getParameterMap());

         StringBuffer receivingURL = new StringBuffer(returnToUrl());
         String queryString = httpReq.getQueryString();
         if (queryString != null && queryString.length() > 0)
            receivingURL.append("?").append(httpReq.getQueryString());

         // verify the response; ConsumerManager needs to be the same
         // (static) instance used to place the authentication request
         VerificationResult verification = manager.verify(receivingURL.toString(), response, discovered);

         // examine the verification result and extract the verified identifier
         Identifier verified = verification.getVerifiedId();
         if (verified != null)
         {
            // AuthSuccess authSuccess =
            // (AuthSuccess) verification.getAuthResponse();

            // if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
            // FetchResponse fetchResp = (FetchResponse) authSuccess
            // .getExtension(AxMessage.OPENID_NS_AX);
            //
            // List emails = fetchResp.getAttributeValues("email");
            // String email = (String) emails.get(0);
            // }

            return verified.getIdentifier();
         }
      }
      catch (OpenIDException e)
      {
         log.warn(e.getMessage());
      }

      return null;
   }

   public void logout() throws ConsumerException
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
      identity = (FliesIdentity) Component.getInstance(FliesIdentity.class, ScopeType.SESSION);
      identityStore = (FliesJpaIdentityStore) Component.getInstance(FliesJpaIdentityStore.class, ScopeType.APPLICATION);
      applicationConfiguration = (ApplicationConfiguration) Component.getInstance(ApplicationConfiguration.class, ScopeType.APPLICATION);
      fliesExternalLoginBean = (FliesExternalLoginBean) Component.getInstance(FliesExternalLoginBean.class, ScopeType.SESSION);
   }
   
   public String loginImmediate()
   {
      if (loginImmediately())
      {
         identity.getCredentials().setInitialized(true);
         identity.setPreAuthenticated(true);
         fliesExternalLoginBean.checkDisabledUser();
         if (!identity.isLoggedIn())
         {
            return "failure";
         }

         if (Events.exists())
            Events.instance().raiseEvent(FliesIdentity.EVENT_LOGIN_SUCCESSFUL);
         
         if (identityStore.isNewUser(identity.getCredentials().getUsername()))
         {
            return "new";
         }

         return "success";
      }
      else
      {
         return "failure";
      }

   }

   public void login(String username)
   {
      try
      {
         String var = "http://" + username + FEDORA_HOST;
         setId(var);
         log.info("openid:" + getId());
         login();
      }
      catch (IOException e)
      {
         log.warn(e.getMessage());
         throw new RuntimeException(e.getMessage());
      }
   }

   public String returnToUrl()
   {
      return applicationConfiguration.getServerPath() + "/openid.seam";
   }

}
