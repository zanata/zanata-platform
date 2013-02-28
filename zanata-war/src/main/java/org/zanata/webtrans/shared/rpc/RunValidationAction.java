package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.ValidationId;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class RunValidationAction extends AbstractWorkspaceAction<RunValidationResult>
{

   private static final long serialVersionUID = 1L;

   private List<ValidationId> validationIds;

   private RunValidationAction()
   {
   }

   public RunValidationAction(List<ValidationId> validationIds)
   {
      this.validationIds = validationIds;
   }

   public List<ValidationId> getValidationIds()
   {
      return validationIds;
   }
}
