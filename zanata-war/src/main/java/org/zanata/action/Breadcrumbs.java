package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("breadcrumbs")
@AutoCreate
@Scope(ScopeType.PAGE)
public class Breadcrumbs implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   @Logger
   Log log;

   private List<Breadcrumb> locations;


   public List<Breadcrumb> getLocations()
   {
      if (locations == null)
      {
         locations = new ArrayList<Breadcrumb>();
      }
      return locations;
   }

   public void clear()
   {
      getLocations().clear();
   }

   public void addLocation(String location, String display)
   {
      getLocations().add(new Breadcrumb(location, display));
   }

   public void addLocation(String location, String display, int index)
   {
      getLocations().add(index, new Breadcrumb(location, display));
   }
   
   public class Breadcrumb
   {
      private String location;
      private String display;
      
      public Breadcrumb(String location, String display)
      {
         this.location = location;
         this.display = display;
      }

      public String getLocation()
      {
         return location;
      }

      public String getDisplay()
      {
         return display;
      }
   }
}
