package net.openl10n.flies.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;
import net.openl10n.flies.webtrans.shared.model.DocumentInfo;
import net.openl10n.flies.webtrans.shared.model.ProjectIterationId;


public class GetDocumentListResult implements Result
{

   private static final long serialVersionUID = 1L;

   private ProjectIterationId projectIterationId;
   private ArrayList<DocumentInfo> documents;

   @SuppressWarnings("unused")
   private GetDocumentListResult()
   {
   }

   public GetDocumentListResult(ProjectIterationId projectIterationId, ArrayList<DocumentInfo> documents)
   {
      this.projectIterationId = projectIterationId;
      this.documents = documents;
   }

   public ArrayList<DocumentInfo> getDocuments()
   {
      return documents;
   }

   public ProjectIterationId getProjectIterationId()
   {
      return projectIterationId;
   }

}
