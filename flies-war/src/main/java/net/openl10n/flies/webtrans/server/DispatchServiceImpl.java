package net.openl10n.flies.webtrans.server;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;
import net.openl10n.flies.webtrans.shared.DispatchService;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.remoting.WebRemote;

@Name("net.openl10n.flies.webtrans.shared.DispatchService")
public class DispatchServiceImpl implements DispatchService
{

   @In
   SeamDispatch seamDispatch;

   @Override
   @WebRemote
   public Result execute(Action<?> action) throws Exception
   {
      return seamDispatch.execute(action);
   }

}
