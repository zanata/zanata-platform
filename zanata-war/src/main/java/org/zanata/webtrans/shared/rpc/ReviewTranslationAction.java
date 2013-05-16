package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ReviewTranslationAction extends AbstractWorkspaceAction<ReviewTranslationResult>
{
   private static final long serialVersionUID = 1L;

   private ReviewResult reviewResult;
   private TransUnitUpdateRequest updateRequest;

   @SuppressWarnings("unused")
   private ReviewTranslationAction()
   {
   }

   private ReviewTranslationAction(TransUnitUpdateRequest updateRequest, ReviewResult reviewResult)
   {
      this.reviewResult = reviewResult;
      this.updateRequest = updateRequest;
   }

   public static ReviewTranslationAction accept(TransUnitUpdateRequest updateRequest)
   {
      return new ReviewTranslationAction(updateRequest, ReviewResult.ACCEPT);
   }

   public TransUnitUpdateRequest getUpdateRequest()
   {
      return updateRequest;
   }
}
