/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.fedorahosted.flies.client.commands;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.ITranslationResources;
import org.fedorahosted.flies.rest.dto.resource.ResourceMeta;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class ListRemoteCommand extends ConfigurableProjectCommand implements FliesCommand
{

   public ListRemoteCommand() throws JAXBException
   {
      super();
   }

   public static void main(String[] args) throws Exception
   {
      ListRemoteCommand me = new ListRemoteCommand();
      ArgsUtil.processArgs(me, args, GlobalOptions.EMPTY);
   }

   @Override
   public String getCommandName()
   {
      return "listremote";
   }

   @Override
   public String getCommandDescription()
   {
      return "Lists all remote documents in the configured Flies project version.";
   }

   @Override
   public void run() throws Exception
   {
      if (getUrl() == null)
         throw new Exception("Flies URL must be specified");
      if (getProject() == null)
         throw new Exception("Project must be specified");
      if (getProjectVersion() == null)
         throw new Exception("Project version must be specified");
      System.out.println("Flies server: " + getUrl());
      System.out.println("Project: " + getProject());
      System.out.println("Version: " + getProjectVersion());
      System.out.println("List of resources:");
      FliesClientRequestFactory factory = new FliesClientRequestFactory(getUrl().toURI(), getUsername(), getKey());
      ITranslationResources translationResources = factory.getTranslationResources(getProject(), getProjectVersion());
      ClientResponse<List<ResourceMeta>> response = translationResources.get();
      ClientUtility.checkResult(response, factory.getTranslationResourcesURI(getProject(), getProjectVersion()));
      List<ResourceMeta> list = response.getEntity();
      for (ResourceMeta doc : list)
      {
         System.out.println(doc.getName());
      }
   }

}
