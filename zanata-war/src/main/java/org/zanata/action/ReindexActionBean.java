package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.security.Restrict;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

@Name("reindexAction")
@Scope(ScopeType.APPLICATION)
@Startup
@Restrict("#{s:hasRole('admin')}")
public class ReindexActionBean implements Serializable
{
   private static final long serialVersionUID = 1L;

   @In
   ReindexAsyncBean reindexAsync;

   public List<ReindexClassOptions> getClasses()
   {
      return reindexAsync.getReindexOptions();
   }

   public void selectAll(boolean selected)
   {
      for (ReindexClassOptions opts : reindexAsync.getReindexOptions())
      {
         opts.setPurge(selected);
         opts.setReindex(selected);
         opts.setOptimize(selected);
      }
   }

   public boolean isPurgeAll()
   {
      for (ReindexClassOptions opts : reindexAsync.getReindexOptions())
      {
         if (!opts.isPurge())
         {
            return false;
         }
      }
      return true;
   }

   public void setPurgeAll(boolean selected)
   {
      for (ReindexClassOptions opts : reindexAsync.getReindexOptions())
      {
         opts.setPurge(selected);
      }
   }

   public boolean isReindexAll()
   {
      for (ReindexClassOptions opts : reindexAsync.getReindexOptions())
      {
         if (!opts.isReindex())
         {
            return false;
         }
      }
      return true;
   }

   public void setReindexAll(boolean selected)
   {
      for (ReindexClassOptions opts : reindexAsync.getReindexOptions())
      {
         opts.setReindex(selected);
      }
   }

   public boolean isOptimizeAll()
   {
      for (ReindexClassOptions opts : reindexAsync.getReindexOptions())
      {
         if (!opts.isOptimize())
         {
            return false;
         }
      }
      return true;
   }

   public void setOptimizeAll(boolean selected)
   {
      for (ReindexClassOptions opts : reindexAsync.getReindexOptions())
      {
         opts.setOptimize(selected);
      }
   }

   public boolean isInProgress()
   {
      return reindexAsync.getProcessHandle().isInProgress();
   }

   public String getCurrentClass()
   {
      return reindexAsync.getCurrentClassName();
   }

   public boolean isError()
   {
      return reindexAsync.getProcessHandle().hasError();
   }

   public int getReindexCount()
   {
      return reindexAsync.getProcessHandle().getMaxProgress();
   }

   public int getReindexProgress()
   {
      return reindexAsync.getProcessHandle().getCurrentProgress();
   }

   public void reindexDatabase()
   {
      if (!reindexAsync.getProcessHandle().isInProgress())
      {
         reindexAsync.startProcess();
      }
   }

   public void cancel()
   {
      reindexAsync.getProcessHandle().stop();
   }

   public boolean isCanceled()
   {
      return reindexAsync.getProcessHandle().shouldStop();
   }

   public boolean isStarted()
   {
      return reindexAsync.getProcessHandle().isStarted();
   }

   // TODO move to common location with ViewAllStatusAction
   private static final PeriodFormatterBuilder PERIOD_FORMATTER_BUILDER =
         new PeriodFormatterBuilder()
               .appendDays().appendSuffix(" day", " days")
               .appendSeparator(", ")
               .appendHours().appendSuffix(" hour", " hours")
               .appendSeparator(", ")
               .appendMinutes().appendSuffix(" min", " mins");

   private String formatTimePeriod( long durationInMillis )
   {
      PeriodFormatter formatter = PERIOD_FORMATTER_BUILDER.toFormatter();
      Period period = new Period( durationInMillis );

      if( period.toStandardMinutes().getMinutes() <= 0 )
      {
         return "less than a minute"; // TODO Localize
      }
      else
      {
         return formatter.print( period.normalizedStandard() );
      }
   }

   public String getElapsedTime()
   {
      return formatTimePeriod(reindexAsync.getProcessHandle().getElapsedTime());
   }

   public String getEstimatedTimeRemaining()
   {
      return formatTimePeriod(reindexAsync.getProcessHandle().getEstimatedTimeRemaining());
   }
}
