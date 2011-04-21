package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;

import net.customware.gwt.dispatch.shared.Result;


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
