package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.TextFlowTarget;

/**
 *
 * @author Hannes Eskebaek
 */
public class GetTargetForLocaleResult implements DispatchResult
{
   private static final long serialVersionUID = 1L;
   private TextFlowTarget target;

   @SuppressWarnings("unused")
   private GetTargetForLocaleResult()
   {
   }

   public GetTargetForLocaleResult(TextFlowTarget target)
   {
      this.target = target;
   }

   public TextFlowTarget getTarget()
   {
      return target;
   }
}
