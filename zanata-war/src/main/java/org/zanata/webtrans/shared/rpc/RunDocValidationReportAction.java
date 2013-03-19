package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.ValidationId;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class RunDocValidationReportAction extends AbstractWorkspaceAction<RunDocValidationReportResult>
{

   private static final long serialVersionUID = 1L;

   private List<ValidationId> validationIds;
   private Long documentId;

   @SuppressWarnings("unused")
   private RunDocValidationReportAction()
   {
   }

   public RunDocValidationReportAction(List<ValidationId> validationIds, Long documentId)
   {
      this.validationIds = validationIds;
      this.documentId = documentId;
   }

   public List<ValidationId> getValidationIds()
   {
      return validationIds;
   }

   public Long getDocumentId()
   {
      return documentId;
   }
}
