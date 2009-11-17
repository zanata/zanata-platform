package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

public interface WorkspaceAction<R extends Result> extends Action<R>{
	
	ProjectContainerId getProjectContainerId();
	LocaleId getLocaleId();
	
}
