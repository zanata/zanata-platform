package org.zanata.action;

/**
 * Stores options for the lucene indexing
 * 
 * @author David Mason, damason@redhat.com
 */
public class ReindexClassOptions
{
   private Class<?> clazz;
   private boolean purge = false;
   private boolean reindex = false;
   private boolean optimize = false;

   public ReindexClassOptions(Class<?> indexableClass)
   {
      clazz = indexableClass;
   }

   public String getClassName()
   {
      return clazz.getSimpleName();
   }

   public boolean isPurge()
   {
      return purge;
   }

   public void setPurge(boolean shouldPurge)
   {
      this.purge = shouldPurge;
   }

   public boolean isReindex()
   {
      return reindex;
   }

   public void setReindex(boolean shouldReindex)
   {
      this.reindex = shouldReindex;
   }

   public boolean isOptimize()
   {
      return optimize;
   }

   public void setOptimize(boolean optimize)
   {
      this.optimize = optimize;
   }

   public void setSelectAll(boolean selectAll)
   {
      setPurge(selectAll);
      setReindex(selectAll);
      setOptimize(selectAll);
   }

   /**
    * Returns true only if all other boolean properties (purge, reindex, optimize) are true
    * @return
    */
   public boolean getSelectAll()
   {
      return isPurge() && isReindex() && isOptimize();
   }


}
