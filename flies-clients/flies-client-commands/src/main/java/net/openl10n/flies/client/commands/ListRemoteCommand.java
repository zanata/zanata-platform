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
package net.openl10n.flies.client.commands;

import java.util.List;

import net.openl10n.flies.rest.client.ClientUtility;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;

import org.jboss.resteasy.client.ClientResponse;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class ListRemoteCommand implements FliesCommand
{

   private final ConfigurableProjectOptions opts;

   public ListRemoteCommand(ConfigurableProjectOptions opts)
   {
      this.opts = opts;
   }

   // public static void main(String[] args)
   // {
   // ConfigurableProjectOptionsImpl opts = new
   // ConfigurableProjectOptionsImpl();
   // ListRemoteCommand me = new ListRemoteCommand(opts);
   // ArgsUtil.processArgs(me, args, opts);
   // }

   @Override
   public void run() throws Exception
   {
      if (opts.getUrl() == null)
         throw new Exception("Flies URL must be specified");
      if (opts.getProject() == null)
         throw new Exception("Project must be specified");
      if (opts.getProjectVersion() == null)
         throw new Exception("Project version must be specified");
      System.out.println("Flies server: " + opts.getUrl());
      System.out.println("Project: " + opts.getProject());
      System.out.println("Version: " + opts.getProjectVersion());
      System.out.println("List of resources:");
      FliesClientRequestFactory factory = new FliesClientRequestFactory(opts.getUrl().toURI(), opts.getUsername(), opts.getKey());
      ITranslationResources translationResources = factory.getTranslationResources(opts.getProject(), opts.getProjectVersion());
      ClientResponse<List<ResourceMeta>> response = translationResources.get(null);
      ClientUtility.checkResult(response, factory.getTranslationResourcesURI(opts.getProject(), opts.getProjectVersion()));
      List<ResourceMeta> list = response.getEntity();
      for (ResourceMeta doc : list)
      {
         System.out.println(doc.getName());
      }
   }

}
