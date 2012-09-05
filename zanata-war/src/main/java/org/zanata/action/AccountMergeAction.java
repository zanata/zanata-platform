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
package org.zanata.action;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.security.FedoraOpenId;
import org.zanata.security.openid.OpenIdAuthCallback;
import org.zanata.security.openid.OpenIdAuthenticationResult;
import org.zanata.security.openid.OpenIdProviderType;
import org.zanata.service.RegisterService;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("accountMergeAction")
@Scope(ScopeType.PAGE)
public class AccountMergeAction
{
   @In(value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;

   @In
   private FedoraOpenId fedoraOpenId;

   @In
   private RegisterService registerServiceImpl;

   @Getter
   @Setter
   private String username;

   @In(required = false, scope = ScopeType.SESSION)
   @Out(required = false, scope = ScopeType.SESSION)
   @Getter
   private HAccount obsoleteAccount;

   private OpenIdProviderType providerType;


   public String getProviderType()
   {
      return providerType != null ? providerType.toString() : "";
   }

   public void setProviderType(String providerType)
   {
      try
      {
         this.providerType = OpenIdProviderType.valueOf(providerType);
      }
      catch (IllegalArgumentException e)
      {
         this.providerType = OpenIdProviderType.Generic;
      }
   }

   public void loginToMergingAccount()
   {
      fedoraOpenId.setProvider( providerType );
      fedoraOpenId.login( username, new AccountMergeAuthCallback() );
   }

   public boolean isAccountSelected()
   {
      return obsoleteAccount != null;
   }

   public void mergeAccounts()
   {
      registerServiceImpl.mergeAccounts(authenticatedAccount, obsoleteAccount);
      obsoleteAccount = null; // reset the obsolete account
      FacesMessages.instance().add("Your accounts have been merged.");
   }

   public void cancel()
   {
      // see pages.xml
      obsoleteAccount = null;
   }

   private class AccountMergeAuthCallback implements OpenIdAuthCallback
   {
      @Override
      public void afterOpenIdAuth(OpenIdAuthenticationResult result)
      {
         if( result.isAuthenticated() )
         {
            AccountDAO accountDAO = (AccountDAO)Component.getInstance(AccountDAO.class);
            HAccount account = accountDAO.getByCredentialsId( result.getAuthenticatedId() );
            Contexts.getSessionContext().set("obsoleteAccount", account); // Outject the account

            if( obsoleteAccount == null )
            {
               FacesMessages.instance().add("Could not find an account for that user.");
            }
         }
         else
         {
            FacesMessages.instance().add("Unable to authenticate that account.");
         }
      }

      @Override
      public String getRedirectToUrl()
      {
         return "/profile/merge_account.seam";
      }
   }
}
