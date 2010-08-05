package org.fedorahosted.flies.client.ant.po;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.fedorahosted.flies.client.command.FliesCommand;

public abstract class FliesTask extends Task implements FliesCommand
{

   @Override
   public void execute() throws BuildException
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         // make sure RESTEasy classes will be found:
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         initConfig();
         run();
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
      }
   }

   @Override
   public void initConfig()
   {
   }

   @Override
   public void log(String msg)
   {
      super.log(msg + "\n\n");
   }

}
