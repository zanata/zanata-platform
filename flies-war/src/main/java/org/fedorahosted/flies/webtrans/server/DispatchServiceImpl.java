package org.fedorahosted.flies.webtrans.server;


import org.fedorahosted.flies.gwt.common.DispatchService;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.Identity;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

@Name("org.fedorahosted.flies.gwt.common.DispatchService")
public class DispatchServiceImpl implements DispatchService {
	
	@In 
	GwtActionDispatcher gwtActionDispatcher;
	
	@Override
	@WebRemote
	public Result execute(Action<?> action) throws Exception {
		return gwtActionDispatcher.execute(action);
	}
	
	

}
