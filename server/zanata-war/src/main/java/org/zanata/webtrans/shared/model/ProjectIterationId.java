package org.zanata.webtrans.shared.model;

import java.io.Serializable;

public class ProjectIterationId implements Serializable
{

   private static final long serialVersionUID = 1L;

   private String projectSlug;
   private String iterationSlug;

   // for GWT
   @SuppressWarnings("unused")
   private ProjectIterationId()
   {
   }

   public ProjectIterationId(String projectSlug, String iterationSlug)
   {
      this.projectSlug = projectSlug;
      this.iterationSlug = iterationSlug;
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public String getIterationSlug()
   {
      return iterationSlug;
   }

   @Override
   public String toString()
   {
      return projectSlug + "/" + iterationSlug;
   }

   @Override
   public int hashCode()
   {
      return (projectSlug + "/" + iterationSlug).hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
         return true;
      if (obj == null)
         return false;
      if (obj instanceof ProjectIterationId)
      {
         ProjectIterationId other = (ProjectIterationId) obj;
         return other.projectSlug == projectSlug && other.iterationSlug == iterationSlug;
      }
      return false;
   }
}
