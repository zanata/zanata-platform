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
   private DocumentId docId; 

   @SuppressWarnings("unused")
   private RunDocValidationAction()
   {
   }

   public RunDocValidationAction(List<ValidationId> validationIds, DocumentId docId)
   {
      this.validationIds = validationIds;
      this.docId = docId;
   }

   public List<ValidationId> getValidationIds()
   {
      return validationIds;
   }

   public DocumentId getDocId()
   {
      return docId;
   }
}
