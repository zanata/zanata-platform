package net.openl10n.flies.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.model.HLocale;
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

import org.apache.commons.lang.StringUtils;
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
   private LocaleService localeServiceImpl;

   @Override
   public UpdateTransUnitResult execute(UpdateTransUnit action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();
      log.info("Updating TransUnit {0}: locale {1}, state {2}, content '{3}'", action.getTransUnitId(), action.getWorkspaceId().getLocaleId(), action.getContentState(), action.getContent());

      HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, action.getTransUnitId().getValue());
      LocaleId locale = action.getWorkspaceId().getLocaleId();
      HLocale hLocale = localeServiceImpl.getSupportedLanguageByLocale(locale);
      HTextFlowTarget target = hTextFlow.getTargets().get(hLocale);
      ContentState prevStatus = ContentState.New;
      if (target == null)
      {
         target = new HTextFlowTarget(hTextFlow, hLocale);
         target.setVersionNum(0); // this will be incremented when content is
                                  // set (below)
         hTextFlow.getTargets().put(hLocale, target);
      }
      else
      {
         prevStatus = target.getState();
      }

      if (action.getContentState() == ContentState.New && StringUtils.isNotEmpty(action.getContent()))
      {
         log.error("invalid ContentState New for TransUnit {0} with content '{1}', assuming NeedReview", action.getTransUnitId(), action.getContent());
         target.setState(ContentState.NeedReview);
      }
      else if (action.getContentState() != ContentState.New && StringUtils.isEmpty(action.getContent()))
      {
         log.error("invalid ContentState {0} for empty TransUnit {1}, assuming New", action.getContentState(), action.getTransUnitId());
         target.setState(ContentState.New);
      }
      else
      {
         target.setState(action.getContentState());
      }
      if (!StringUtils.equals(action.getContent(), target.getContent()))
      {
         target.setContent(action.getContent());
         target.setVersionNum(target.getVersionNum() + 1);
         // TODO target.setLastModifiedBy(currentUser)
      }

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