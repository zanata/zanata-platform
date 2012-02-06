package org.zanata.webtrans.shared.rpc;

import org.zanata.common.EntityStatus;


public class ProjectIterationUpdate implements SessionEventData, HasProjectIterationUpdateData
{

   private static final long serialVersionUID = 1L;

   private String projectSlug;
   private String projectIterationSlug;
   private EntityStatus projectStatus;
   private EntityStatus projectIterationStatus;

   @SuppressWarnings("unused")
   private ProjectIterationUpdate()
   {
   }

   public ProjectIterationUpdate(String projectSlug, EntityStatus projectStatus, String projectIterationSlug, EntityStatus projectIterationStatus)
   {
      this.projectSlug = projectSlug;
      this.projectStatus = projectStatus;
      this.projectIterationSlug = projectIterationSlug;
      this.projectIterationStatus = projectIterationStatus;
   }


   @Override
   public String getProjectSlug()
   {
      return projectSlug;
   }

   @Override
   public EntityStatus getProjectStatus()
   {
      return projectStatus;
   }

   @Override
   public String getProjectIterationSlug()
   {
      return projectIterationSlug;
   }

   @Override
   public EntityStatus getProjectIterationStatus()
   {
      return projectIterationStatus;
   }
}
