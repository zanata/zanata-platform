package net.openl10n.flies.action;

import java.util.HashSet;
import java.util.Set;

import net.openl10n.flies.model.HCommunity;
import net.openl10n.flies.model.HIterationProject;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.annotations.Indexed;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.deployment.StandardDeploymentStrategy;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Name("adminAction")
@Scope(ScopeType.APPLICATION)
@Startup
@Restrict("#{s:hasRole('admin')}")
public class AdminActionBean
{

   private static final int BATCH_SIZE = 500;

   @Logger
   private Log log;

   @In
   FullTextSession session;

   private Set<Class<?>> indexables = new HashSet<Class<?>>();

   @Create
   public void create()
   {
      indexables.addAll(StandardDeploymentStrategy.instance().getAnnotatedClasses().get(Indexed.class.getName()));
      indexables.add(HCommunity.class);
      indexables.add(HIterationProject.class);
   }

   /*
    * TODO make it an @Asynchronous call and have some boolean isRunning method
    * to disable the button if the job is already running
    */

   public void reindexDatabase()
   {
      log.info("Re-indexing started");

      // reindex all @Indexed entities
      for (Class<?> clazz : indexables)
      {
         log.info("Reindexing class " + clazz.getName());
         reindex(clazz);
      }

      log.info("Re-indexing finished");

      // TODO: this is a global action - not for a specific user
      // should have some sort of global status on this one
      FacesMessages.instance().add("Re-indexing finished");
   }

   private void reindex(Class<?> clazz)
   {
      log.info("Re-indexing {0}", clazz);
      try
      {
         session.purgeAll(clazz);
         session.setFlushMode(FlushMode.MANUAL);
         session.setCacheMode(CacheMode.IGNORE);
         Transaction transaction = session.beginTransaction();
         // Scrollable results will avoid loading too many objects in memory
         ScrollableResults results = session.createCriteria(clazz).setFetchSize(BATCH_SIZE).scroll(ScrollMode.FORWARD_ONLY);
         int index = 0;
         while (results.next())
         {
            index++;
            session.index(results.get(0)); // index each element
            if (index % BATCH_SIZE == 0)
            {
               session.flushToIndexes(); // apply changes to indexes
               session.clear(); // clear since the queue is processed
            }
         }
         results.close();
         transaction.commit();
      }
      catch (Exception e)
      {
         log.warn("Unable to index objects of type {0}", e, clazz.getName());
      }
   }

}
