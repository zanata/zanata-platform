package org.zanata.client.commands;

public interface AppAbortStrategy
{
   void abort(Throwable e);
}