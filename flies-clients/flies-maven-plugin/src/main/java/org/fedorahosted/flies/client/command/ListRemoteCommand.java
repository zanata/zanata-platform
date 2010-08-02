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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.fedorahosted.flies.rest.client.FliesClientRequestFactory;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class ListRemoteCommand extends ConfigurableProjectCommand
{

   public ListRemoteCommand() throws JAXBException
   {
      super();
   }

   @Override
   public void run() throws MalformedURLException, URISyntaxException
   {
      // TODO ensure url ends with '/'
      FliesClientRequestFactory factory = new FliesClientRequestFactory(getUsername(), getKey());
      String spec = "seam/resource/restv1/projects/p/" + getProjectSlug() + "/iterations/i/" + getVersionSlug() + "/r";
      // URL resourceURL = new URL(getUrl(), ".");
      URL resourceURL = new URL(getUrl(), spec);
      System.out.println(resourceURL);
      factory.getTranslationResourcesResource(resourceURL.toURI());
   }

}
