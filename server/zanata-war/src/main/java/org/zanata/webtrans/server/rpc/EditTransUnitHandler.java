package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.rpc.EditingTranslationAction;
import org.zanata.webtrans.shared.rpc.EditingTranslationResult;

@Name("webtrans.gwt.EditTransUnitHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(EditingTranslationAction.class)
public class EditTransUnitHandler extends AbstractActionHandler<EditingTranslationAction, EditingTranslationResult>
{

   @In
   Session session;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @Override
   public EditingTranslationResult execute(EditingTranslationAction action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();

      return new EditingTranslationResult(true);
   }

   @Override
   public void rollback(EditingTranslationAction action, EditingTranslationResult result, ExecutionContext context) throws ActionException
   {
   }
}
