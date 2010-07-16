package org.fedorahosted.flies.action;

import org.fedorahosted.flies.model.HFliesLocale;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

@Name("localeHome")
public class LocaleHome extends EntityHome<HFliesLocale>
{
   @RequestParameter
   private String id;

   @Override
   public Object getId()
   {
      if (id == null)
      {
         return super.getId();
      }

      return id;
   }

   @Override
   @Begin
   public void create()
   {
      super.create();
   }

}
