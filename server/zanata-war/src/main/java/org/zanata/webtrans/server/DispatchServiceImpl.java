package org.zanata.webtrans.server;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.zanata.webtrans.shared.DispatchService;

@Name("org.zanata.webtrans.shared.DispatchService")
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
   
   @Override
   @WebRemote
   public void rollback(Action<Result> action, Result result) throws Exception
   {
      seamDispatch.rollback(action, result);
   }

}
