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
import org.zanata.model.HEditorOption;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;
import org.zanata.webtrans.shared.rpc.NavOption;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import static org.zanata.model.HEditorOption.OptionName.*;

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
      LoadOptionsResult result = new LoadOptionsResult();
      HAccount account = accountDAO.findById(authenticatedAccount.getId(), true);
      Map<String,HEditorOption> editorOptions = account.getEditorOptions();

      if( editorOptions.containsKey(DisplayButtons.getPersistentName()) )
      {
         result.setShowEditorButtons( editorOptions.get( DisplayButtons.getPersistentName() ).getValueAsBoolean() );
      }
      else
      {
         result.setShowEditorButtons(true);
      }

      if( editorOptions.containsKey(EnterSavesApproved.getPersistentName()) )
      {
         result.setEnterKeySavesImmediately( editorOptions.get( EnterSavesApproved.getPersistentName() ).getValueAsBoolean() );
      }
      else
      {
         result.setEnterKeySavesImmediately(false);
      }

      if( editorOptions.containsKey(Navigation.getPersistentName()) )
      {
         result.setNavOption( NavOption.valueOf(editorOptions.get(Navigation.getPersistentName()).getValue()) );
      }
      else
      {
         result.setNavOption( NavOption.FUZZY_UNTRANSLATED );
      }

      if( editorOptions.containsKey(PageSize.getPersistentName()) )
      {
         result.setPageSize( editorOptions.get( PageSize.getPersistentName() ).getValueAsInt() );
      }
      else
      {
         result.setPageSize(25);
      }

      if( editorOptions.containsKey(ShowErrors.getPersistentName()) )
      {
         result.setShowErrors( editorOptions.get( ShowErrors.getPersistentName() ).getValueAsBoolean() );
      }
      else
      {
         result.setShowErrors(false);
      }

      if( editorOptions.containsKey(TranslatedMessageFilter.getPersistentName()) )
      {
         result.setFilterByTranslated( editorOptions.get( TranslatedMessageFilter.getPersistentName() ).getValueAsBoolean() );
      }
      else
      {
         result.setFilterByTranslated(false);
      }

      if( editorOptions.containsKey(NeedReviewMessageFilter.getPersistentName()) )
      {
         result.setFilterByNeedReview( editorOptions.get( NeedReviewMessageFilter.getPersistentName() ).getValueAsBoolean() );
      }
      else
      {
         result.setFilterByNeedReview(false);
      }

      if( editorOptions.containsKey(UntranslatedMessageFilter.getPersistentName()) )
      {
         result.setFilterByUntraslated( editorOptions.get( UntranslatedMessageFilter.getPersistentName() ).getValueAsBoolean() );
      }
      else
      {
         result.setFilterByUntraslated(false);
      }

      return result;
   }

   @Override
   public void rollback(LoadOptionsAction action, LoadOptionsResult result, ExecutionContext context) throws ActionException
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }
}
