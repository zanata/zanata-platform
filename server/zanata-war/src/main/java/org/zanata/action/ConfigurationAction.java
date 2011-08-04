/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.Namespaces;
import org.zanata.model.HLocale;
import org.zanata.service.LocaleService;

@Name("configurationAction")
@Scope(ScopeType.EVENT)
public class ConfigurationAction implements Serializable
{
   private static final long serialVersionUID = 1L;
   @RequestParameter
   private String iterationSlug;
   @RequestParameter
   private String projectSlug;
   private static String FILE_NAME = "zanata.xml";
   @Logger
   Log log;
   @In
   LocaleService localeServiceImpl;
   @In
   ApplicationConfiguration applicationConfiguration;

   public void getData()
   {
      HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
      response.setContentType("application/xml");
      response.addHeader("Content-disposition", "attachment; filename=\"" + FILE_NAME + "\"");
      response.setCharacterEncoding("UTF-8");
      try
      {
         ServletOutputStream os = response.getOutputStream();
         StringBuilder var = new StringBuilder(
               "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
               "<config xmlns=\"" + Namespaces.ZANATA_CONFIG + "\">\n");
         var.append("  <url>" + applicationConfiguration.getServerPath() + "/</url>\n");
         var.append("  <project>" + projectSlug + "</project>\n");
         var.append("  <project-version>" + iterationSlug + "</project-version>\n\n");

         List<HLocale> locales = localeServiceImpl.getSupportedLangugeByProjectIteration(projectSlug, iterationSlug);
         HLocale source = localeServiceImpl.getSourceLocale(projectSlug, iterationSlug);
         
         if(locales!=null){
            boolean first=true;
            for(HLocale op: locales){
               if(!op.equals(source)){
                  if (first)
                  {
                     var.append("  <locales>\n");
                  }
                  var.append("    <locale>" + op.getLocaleId().getId() + "</locale>\n");
                  first = false;
               }
            }
            if (!first)
            {
               var.append("  </locales>\n\n");
            }
         }
         
         var.append("</config>\n");

         os.write(var.toString().getBytes());
         os.flush();
         os.close();
         FacesContext.getCurrentInstance().responseComplete();
      }
      catch (Exception e)
      {
         log.error("Failure : " + e.toString() + "\n");
      }
   }

}
