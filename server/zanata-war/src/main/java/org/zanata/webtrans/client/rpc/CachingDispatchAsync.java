package org.zanata.webtrans.client.rpc;

import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import net.customware.gwt.dispatch.client.DispatchAsync;

public interface CachingDispatchAsync extends DispatchAsync
{

   void setWorkspaceContext(WorkspaceContext workspaceContext);

   void setIdentity(Identity identity);

}
