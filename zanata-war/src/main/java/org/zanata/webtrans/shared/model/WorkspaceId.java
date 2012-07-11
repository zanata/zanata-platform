package org.zanata.webtrans.shared.model;

import org.zanata.common.LocaleId;

import com.google.gwt.user.client.rpc.IsSerializable;


public final class WorkspaceId implements IsSerializable
{
   private ProjectIterationId projectIterationId;
   private LocaleId localeId;

   // for GWT
   @SuppressWarnings("unused")
   private WorkspaceId()
   {
   }

   public WorkspaceId(ProjectIterationId projectIterationId, LocaleId localeId)
   {
      if (projectIterationId == null)
         throw new IllegalArgumentException("projectIterationId");
      if (localeId == null)
         throw new IllegalArgumentException("localeId");

      this.projectIterationId = projectIterationId;
      this.localeId = localeId;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (!(obj instanceof WorkspaceId))
         return false;
      WorkspaceId other = (WorkspaceId) obj;
      return (other.localeId.equals(localeId) && other.projectIterationId.equals(projectIterationId));
   }

   @Override
   public int hashCode()
   {
      int hash = 1;
      hash = hash * 31 + localeId.hashCode();
      hash = hash * 31 + projectIterationId.hashCode();
      return hash;
   }

   public LocaleId getLocaleId()
   {
      return localeId;
   }

   public ProjectIterationId getProjectIterationId()
   {
      return projectIterationId;
   }

   @Override
   public String toString()
   {
      return localeId.toString() + ":" + projectIterationId;
   }

}
