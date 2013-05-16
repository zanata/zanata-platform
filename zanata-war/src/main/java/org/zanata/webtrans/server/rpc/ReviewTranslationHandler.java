package org.zanata.webtrans.server.rpc;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.ReviewTranslationAction;
import org.zanata.webtrans.shared.rpc.ReviewTranslationResult;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("webtrans.gwt.ReviewTranslationHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(ReviewTranslationAction.class)
@Slf4j
public class ReviewTranslationHandler extends AbstractActionHandler<ReviewTranslationAction, ReviewTranslationResult>
{
   @In(value = "webtrans.gwt.UpdateTransUnitHandler", create = true)
   private UpdateTransUnitHandler updateTransUnitHandler;

   @Override
   public ReviewTranslationResult execute(ReviewTranslationAction action, ExecutionContext context) throws ActionException
   {
      //TODO security check to see if this is from a reviewer

      UpdateTransUnitResult result = updateTransUnitHandler.execute(new UpdateTransUnit(action.getUpdateRequest(), TransUnitUpdated.UpdateType.WebEditorSave), context);
      // TODO what do we do with the update result
      return new ReviewTranslationResult();
   }

   @Override
   public void rollback(ReviewTranslationAction action, ReviewTranslationResult result, ExecutionContext context) throws ActionException
   {
   }
}
