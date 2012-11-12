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
package org.zanata.webtrans.server.rpc;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountOption;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;
import org.zanata.webtrans.shared.rpc.NavOption;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import static org.zanata.model.HAccountOption.OptionName.*;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("webtrans.gwt.LoadOptionsHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(LoadOptionsAction.class)
public class LoadOptionsHandler extends AbstractActionHandler<LoadOptionsAction, LoadOptionsResult>
{
   @In(value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;

   @In
   private AccountDAO accountDAO;


   @Override
   public LoadOptionsResult execute(LoadOptionsAction action, ExecutionContext context) throws ActionException
   {
      UserConfigHolder configHolder = new UserConfigHolder();
      HAccount account = accountDAO.findById(authenticatedAccount.getId(), true);
      Map<String,HAccountOption> editorOptions = account.getEditorOptions();

      if( editorOptions.containsKey(DisplayButtons.getPersistentName()) )
      {
         configHolder.setDisplayButtons(editorOptions.get(DisplayButtons.getPersistentName()).getValueAsBoolean());
      }

      if( editorOptions.containsKey(EnterSavesApproved.getPersistentName()) )
      {
         configHolder.setEnterSavesApproved(editorOptions.get(EnterSavesApproved.getPersistentName()).getValueAsBoolean());
      }

      if( editorOptions.containsKey(Navigation.getPersistentName()) )
      {
         configHolder.setNavOption( NavOption.valueOf(editorOptions.get(Navigation.getPersistentName()).getValue()) );
      }

      if( editorOptions.containsKey(PageSize.getPersistentName()) )
      {
         configHolder.setPageSize( editorOptions.get( PageSize.getPersistentName() ).getValueAsInt() );
      }

      if( editorOptions.containsKey(ShowErrors.getPersistentName()) )
      {
         configHolder.setShowError(editorOptions.get(ShowErrors.getPersistentName()).getValueAsBoolean());
      }

      if( editorOptions.containsKey(TranslatedMessageFilter.getPersistentName()) )
      {
         configHolder.setFilterByTranslated( editorOptions.get( TranslatedMessageFilter.getPersistentName() ).getValueAsBoolean() );
      }

      if( editorOptions.containsKey(NeedReviewMessageFilter.getPersistentName()) )
      {
         configHolder.setFilterByNeedReview( editorOptions.get( NeedReviewMessageFilter.getPersistentName() ).getValueAsBoolean() );
      }

      if( editorOptions.containsKey(UntranslatedMessageFilter.getPersistentName()) )
      {
         configHolder.setFilterByUntranslated(editorOptions.get(UntranslatedMessageFilter.getPersistentName()).getValueAsBoolean());
      }

      LoadOptionsResult result = new LoadOptionsResult();
      result.setConfiguration( configHolder.getState() );
      return result;
   }

   @Override
   public void rollback(LoadOptionsAction action, LoadOptionsResult result, ExecutionContext context) throws ActionException
   {
   }
}
