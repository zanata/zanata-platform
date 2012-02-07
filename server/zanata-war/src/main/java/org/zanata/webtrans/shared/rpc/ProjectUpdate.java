package org.zanata.webtrans.shared.rpc;

import org.zanata.common.EntityStatus;


public class ProjectUpdate implements SessionEventData, HasProjectUpdateData
{

   private static final long serialVersionUID = 1L;

   private String projectSlug;
   private EntityStatus status;

   @SuppressWarnings("unused")
   private ProjectUpdate()
   {
   }

   public ProjectUpdate(String projectSlug, EntityStatus status)
   {
      this.projectSlug = projectSlug;
      this.status = status;
   }


   @Override
   public String getProjectSlug()
   {
      return projectSlug;
   }

   @Override
   public EntityStatus getProjectStatus()
   {
      return status;
   }
}
