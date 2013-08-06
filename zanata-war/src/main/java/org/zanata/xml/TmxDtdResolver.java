/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

package org.zanata.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TmxDtdResolver implements XMLResolver, EntityResolver
{

   @Override
   public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException
   {
      if ("http://www.lisa.org/tmx/tmx14.dtd".equals(systemID))
      {
         InputStream stream = getClass().getResourceAsStream("tmx14.dtd");
         return stream;
      }
      else
      {
         return null;
      }
   }

   @Override
   public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
   {
      if ("http://www.lisa.org/tmx/tmx14.dtd".equals(systemId))
      {
         InputStream stream = getClass().getResourceAsStream("tmx14.dtd");
         return new InputSource(stream);
      }
      else
      {
         return null;
      }
   }

}
