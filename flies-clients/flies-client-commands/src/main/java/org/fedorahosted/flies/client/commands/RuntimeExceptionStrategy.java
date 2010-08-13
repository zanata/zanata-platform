package org.fedorahosted.flies.client.commands;

public class RuntimeExceptionStrategy implements AppAbortStrategy
{
   @Override
   public void abort()
   {
      throw new RuntimeException("abort called");
   }
}