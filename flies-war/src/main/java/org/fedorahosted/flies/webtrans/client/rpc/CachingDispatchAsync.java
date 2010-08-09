package org.fedorahosted.flies.webtrans.client.rpc;

import net.customware.gwt.dispatch.client.DispatchAsync;

import org.fedorahosted.flies.webtrans.shared.auth.Identity;
import org.fedorahosted.flies.webtrans.shared.model.WorkspaceContext;

public interface CachingDispatchAsync extends DispatchAsync
{

   void setWorkspaceContext(WorkspaceContext workspaceContext);

   void setIdentity(Identity identity);

}
