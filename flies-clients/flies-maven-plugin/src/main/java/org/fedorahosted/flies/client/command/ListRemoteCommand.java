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
package org.fedorahosted.flies.client.command;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.fedorahosted.flies.rest.client.ClientUtility;
import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;
import org.fedorahosted.flies.rest.client.ITranslationResources;
import org.fedorahosted.flies.rest.dto.resource.ResourceMeta;
import org.jboss.resteasy.client.ClientResponse;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class ListRemoteCommand extends ConfigurableProjectCommand
{
   private static final Logger log = LoggerFactory.getLogger(ListRemoteCommand.class);


   public ListRemoteCommand() throws JAXBException
   {
      super();
   }

   @Override
   public void run() throws Exception
   {
      FliesClientRequestFactory factory = new FliesClientRequestFactory(getUrl().toURI(), getUsername(), getKey());
      ITranslationResources translationResources = factory.getTranslationResources(getProjectSlug(), getVersionSlug());
      ClientResponse<List<ResourceMeta>> response = translationResources.get();
      ClientUtility.checkResult(response, factory.getTranslationResourcesURI(getProjectSlug(), getVersionSlug()));
      List<ResourceMeta> list = response.getEntity();
      System.out.println(list);
   }

   public static void main(String[] args) throws Exception
   {
      ListRemoteCommand me = new ListRemoteCommand();
      CmdLineParser parser = new CmdLineParser(me);
      try
      {
         parser.setUsageWidth(Integer.parseInt(System.getenv("COLUMNS")));
      }
      catch (Exception e)
      {
         parser.setUsageWidth(120);
      }
      try
      {
         parser.parseArgument(args);
      }
      catch (CmdLineException e)
      {
         if (!me.getHelp() && args.length != 0)
         {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            System.exit(1);
         }
      }
      if (me.getHelp() || args.length == 0)
      {
         parser.printUsage(System.out);
      }
      else
      {
         me.initConfig();
         me.run();
      }
   }

}
