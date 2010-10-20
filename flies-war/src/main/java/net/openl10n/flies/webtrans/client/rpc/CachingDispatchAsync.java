package net.openl10n.flies.webtrans.client.rpc;

import net.openl10n.flies.webtrans.shared.auth.Identity;
import net.openl10n.flies.webtrans.shared.model.WorkspaceContext;

import net.customware.gwt.dispatch.client.DispatchAsync;

public interface CachingDispatchAsync extends DispatchAsync
{

   void setWorkspaceContext(WorkspaceContext workspaceContext);

   void setIdentity(Identity identity);

}
