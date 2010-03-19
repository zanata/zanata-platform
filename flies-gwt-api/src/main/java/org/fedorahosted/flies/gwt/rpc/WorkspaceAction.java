package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.common.WorkspaceId;

public interface WorkspaceAction<R extends Result> extends DispatchAction<R>{
	
	WorkspaceId getWorkspaceId();
	
}
