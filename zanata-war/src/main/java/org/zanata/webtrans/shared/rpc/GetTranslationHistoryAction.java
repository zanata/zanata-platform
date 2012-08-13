package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.TransUnitId;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GetTranslationHistoryAction extends AbstractWorkspaceAction<GetTranslationHistoryResult>
{
   private TransUnitId transUnitId;

   public GetTranslationHistoryAction(TransUnitId transUnitId)
   {
      this.transUnitId = transUnitId;
   }

   @SuppressWarnings("unused")
   private GetTranslationHistoryAction()
   {
   }

   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }
}
