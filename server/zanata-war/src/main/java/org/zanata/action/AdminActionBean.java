package org.zanata.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.security.Restrict;

@Name("adminAction")
@Scope(ScopeType.APPLICATION)
@Startup
@Restrict("#{s:hasRole('admin')}")
public class AdminActionBean
{

   @In
   ReindexAsyncBean reindexAsync;

   public boolean isReindexing()
   {
      return reindexAsync.isReindexing();
   }

   public boolean isReindexError()
   {
      return reindexAsync.hasError();
   }

   public int getReindexCount()
   {
      return reindexAsync.getObjectCount();
   }

   public int getReindexProgress()
   {
      return reindexAsync.getObjectProgress();
   }

   public void reindexDatabase()
   {
      if (!reindexAsync.isReindexing())
      {
         reindexAsync.prepareReindex();
         reindexAsync.startReindex();
      }
   }

}
