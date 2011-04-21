package org.zanata.client.commands;

public class RuntimeExceptionStrategy implements AppAbortStrategy
{
   @Override
   public void abort(Throwable e)
   {
      throw new RuntimeException("abort called", e);
   }
}