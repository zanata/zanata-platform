package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ValidationId;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class RunDocValidationAction extends AbstractWorkspaceAction<RunDocValidationResult>
{

   private static final long serialVersionUID = 1L;

   private List<ValidationId> validationIds;
   private List<DocumentId> documentIds;

   @SuppressWarnings("unused")
   private RunDocValidationAction()
   {
   }

   public RunDocValidationAction(List<ValidationId> validationIds, List<DocumentId> documentIds)
   {
      this.validationIds = validationIds;
      this.documentIds = documentIds;
   }

   public List<ValidationId> getValidationIds()
   {
      return validationIds;
   }

   public List<DocumentId> getDocIds()
   {
      return documentIds;
   }
}
