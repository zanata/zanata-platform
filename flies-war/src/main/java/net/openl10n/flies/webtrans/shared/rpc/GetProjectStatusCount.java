package net.openl10n.flies.webtrans.shared.rpc;


// TODO get rid of this
// DocumentListPresenter uses this service to fetch stats for all 
// documents in the workspace, but then it adds them all up
// and discards the individual document stats.
@Deprecated
public class GetProjectStatusCount extends AbstractWorkspaceAction<GetProjectStatusCountResult>
{

   public GetProjectStatusCount()
   {
   }

}
