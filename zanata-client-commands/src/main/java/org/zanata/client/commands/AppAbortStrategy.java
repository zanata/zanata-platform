package org.zanata.client.commands;

public interface AppAbortStrategy
{
   void abort(String msg);
   void abort(Throwable e);
}