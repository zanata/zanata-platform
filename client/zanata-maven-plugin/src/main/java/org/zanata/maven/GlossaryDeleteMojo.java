/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.maven;

import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.commands.glossary.delete.GlossaryDeleteCommand;
import org.zanata.client.commands.glossary.delete.GlossaryDeleteOptions;

/**
 * Delete glossary entry from Zanata.
 * 
 * @goal glossary-delete
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class GlossaryDeleteMojo extends ConfigurableProjectMojo implements GlossaryDeleteOptions
{

   /**
    * Locale of glossary to delete
    * 
    * @parameter expression="${zanata.lang}"
    */
   private String lang;

   /**
    * Delete entire glossaries
    * 
    * @parameter expression="${zanata.allGlossary}" default-value="false"
    */
   private boolean allGlossary = false;


   @Override
   public String getlang()
   {
      return lang;
   }

   @Override
   public boolean getAllGlossary()
   {
      return allGlossary;
   }


   @Override
   public ZanataCommand initCommand()
   {
      return new GlossaryDeleteCommand(this);
   }


}


 