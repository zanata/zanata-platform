package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;


public class GetDocumentListResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private ProjectIterationId projectIterationId;
   private ArrayList<DocumentInfo> documents;

   private int totalDocumentsCount;

   @SuppressWarnings("unused")
   private GetDocumentListResult()
   {
   }

   public GetDocumentListResult(ProjectIterationId projectIterationId, ArrayList<DocumentInfo> documents, int totalDocumentsCount)
   {
      this.projectIterationId = projectIterationId;
      this.documents = documents;
      this.totalDocumentsCount = totalDocumentsCount;
   }

   public ArrayList<DocumentInfo> getDocuments()
   {
      return documents;
   }

   public ProjectIterationId getProjectIterationId()
   {
      return projectIterationId;
   }

   public int getTotalDocumentsCount()
   {
      return totalDocumentsCount;
   }
}
