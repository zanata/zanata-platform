package org.zanata.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Name("adminAction")
@Scope(ScopeType.APPLICATION)
@Startup
@Restrict("#{s:hasRole('admin')}")
public class AdminActionBean
{

   @Logger
   private Log log;

   @In
   ReindexAsyncBean reindexAsync;

   @Create
   public void create()
   {

   }

   /*
    * TODO make it an @Asynchronous call and have some boolean isRunning method
    * to disable the button if the job is already running
    */

   public void reindexDatabase()
   {
      if (reindexAsync.isReindexing())
      {
         FacesMessages.instance().add("Reindexing already in progress, did not start reindexing.");
         return;
      }
      reindexAsync.reindexDatabase();

      // TODO: the following will be replaced with a progress bar and
      // disabling/hiding the reindex button
      FacesMessages.instance().add("Started reindexing. See server log for progress");

      boolean finished = false;
      while (!finished)
      {
         try
         {
            Thread.sleep(5000);
            log.info("Reindexing: {0}, done {1} of {2}", reindexAsync.isReindexing(), reindexAsync.getObjectProgress(), reindexAsync.getObjectCount());
            finished = !reindexAsync.isReindexing();
         }
         catch (Throwable t)
         {
            finished = true;
         }
      }

   }


}
