package net.openl10n.flies.webtrans.shared.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.openl10n.flies.webtrans.shared.model.ProjectIterationId;


public class GetDocumentList implements Action<GetDocumentListResult>
{

   private static final long serialVersionUID = 1L;

   private ProjectIterationId projectIterationId;

   @SuppressWarnings("unused")
   private GetDocumentList()
   {
   }

   public GetDocumentList(ProjectIterationId id)
   {
      this.projectIterationId = id;
   }

   public ProjectIterationId getProjectIterationId()
   {
      return projectIterationId;
   }

   public void setProjectContainerId(ProjectIterationId projectIterationId)
   {
      this.projectIterationId = projectIterationId;
   }

}
