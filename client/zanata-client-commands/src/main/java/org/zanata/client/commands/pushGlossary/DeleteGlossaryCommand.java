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
package org.zanata.client.commands.pushGlossary;

import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.ConfigurableOptions;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class DeleteGlossaryCommand extends ConfigurableCommand
{

   /**
    * @param opts
    */
   public DeleteGlossaryCommand(ConfigurableOptions opts)
   {
      super(opts);
      // TODO Auto-generated constructor stub
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.zanata.client.commands.ZanataCommand#run()
    */
   @Override
   public void run() throws Exception
   {
      // TODO Auto-generated method stub

   }

   // private void deleteTargetLocaleGlossaryFromServer(String transLang)
   // {
   // log.info("deleting glossaries with locale [{}] from server", transLang);
   // ClientResponse<String> response = glossaryResource.deleteGlossary(new
   // LocaleId(transLang));
   // ClientUtility.checkResult(response, uri);
   // }
}


 