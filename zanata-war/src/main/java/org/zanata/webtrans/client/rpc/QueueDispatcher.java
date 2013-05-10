package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class QueueDispatcher<A extends Action<R>, R extends Result> implements HasQueueDispatch<A, R>
{
   private final CachingDispatchAsync dispatcher;

   private ArrayList<A> actionQueue;
   private AsyncCallback<R> callback;

   public QueueDispatcher(final CachingDispatchAsync dispatcher)
   {
      this.dispatcher = dispatcher;
   }

   @Override
   public void setQueueAndExecute(ArrayList<A> actionQueue, AsyncCallback<R> callback)
   {
      this.actionQueue = actionQueue;
      this.callback = callback;
      executeQueue();
   }

   @Override
   public void executeQueue()
   {
      if (!isQueueEmpty())
      {
         executeDispatch(popQueue());
      }
   }

   private void executeDispatch(final Action<R> action)
   {
      dispatcher.execute(action, callback);
   }

   private Action<R> popQueue()
   {
      Action<R> action = actionQueue.get(0);
      actionQueue.remove(0);
      return action;
   }

   @Override
   public boolean isQueueEmpty()
   {
      return actionQueue == null || actionQueue.isEmpty();
   }
}
