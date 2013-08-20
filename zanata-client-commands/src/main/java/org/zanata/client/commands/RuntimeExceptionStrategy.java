package org.zanata.client.commands;

public class RuntimeExceptionStrategy implements AppAbortStrategy
{
   @Override
   public void abort(String msg)
   {
      throw new AppAbortException(msg);
   }
   @Override
   public void abort(Throwable e)
   {
      throw new AppAbortException("abort called", e);
   }
}