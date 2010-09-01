package net.openl10n.flies.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.service.LocaleService;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.server.TranslationWorkspace;
import net.openl10n.flies.webtrans.server.TranslationWorkspaceManager;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.rpc.TransUnitUpdated;
import net.openl10n.flies.webtrans.shared.rpc.UpdateTransUnit;
import net.openl10n.flies.webtrans.shared.rpc.UpdateTransUnitResult;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.UpdateTransUnitHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(UpdateTransUnit.class)
public class UpdateTransUnitHandler extends AbstractActionHandler<UpdateTransUnit, UpdateTransUnitResult>
{

   @Logger
   Log log;

   @In
   Session session;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @In
   LocaleService localeServiceImpl;

   @Override
   public UpdateTransUnitResult execute(UpdateTransUnit action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();
      log.info("Updating TransUnit {0}: locale {1}, state {2}, content '{3}'", action.getTransUnitId(), action.getWorkspaceId().getLocaleId(), action.getContentState(), action.getContent());

      HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, action.getTransUnitId().getValue());
      HTextFlowTarget target = hTextFlow.getTargets().get(action.getWorkspaceId().getLocaleId());
      ContentState prevStatus = ContentState.New;
      if (target == null)
      {
         LocaleId locale = action.getWorkspaceId().getLocaleId();
         if (!localeServiceImpl.localeSupported(locale))
         {
            throw new ActionException("Unsupported Locale: " + locale.getId() + " within this context");
         }
         target = new HTextFlowTarget(hTextFlow, locale);
         hTextFlow.getTargets().put(action.getWorkspaceId().getLocaleId(), target);
      }
      else
      {
         prevStatus = target.getState();
      }
      target.setState(action.getContentState());
      target.setContent(action.getContent());
      // TODO update last modified by
      session.flush();

      TransUnitUpdated event = new TransUnitUpdated(new DocumentId(hTextFlow.getDocument().getId()), action.getTransUnitId(), prevStatus, action.getContentState());

      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());
      workspace.publish(event);

      return new UpdateTransUnitResult(true);
   }

   @Override
   public void rollback(UpdateTransUnit action, UpdateTransUnitResult result, ExecutionContext context) throws ActionException
   {
   }

}