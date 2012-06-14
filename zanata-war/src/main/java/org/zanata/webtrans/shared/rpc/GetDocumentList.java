package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.ProjectIterationId;


public class GetDocumentList extends AbstractWorkspaceAction<GetDocumentListResult>
{

   private static final long serialVersionUID = 1L;

   private ProjectIterationId projectIterationId;
   private List<String> filters;

   @SuppressWarnings("unused")
   private GetDocumentList()
   {
   }

   public GetDocumentList(ProjectIterationId id)
   {
      this(id, null);
   }

   public GetDocumentList(ProjectIterationId id, List<String> filters)
   {
      this.projectIterationId = id;
      this.filters = filters;
   }

   public ProjectIterationId getProjectIterationId()
   {
      return projectIterationId;
   }

   public void setProjectIterationId(ProjectIterationId projectIterationId)
   {
      this.projectIterationId = projectIterationId;
   }

   public List<String> getFilters()
   {
      return filters;
   }

   public void setFilters(List<String> filters)
   {
      this.filters = filters;
   }

}
