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

package org.zanata.model.tm;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TMMetadataHelper
{
   private static final ObjectMapper mapper = new ObjectMapper();

   public static Map<String, String> getTMXMetadata(HasTMMetadata entity)
   {
      String metadataString = entity.getMetadata().get(TMMetadataType.TMX14);
      try
      {
         return mapper.readValue(metadataString, Map.class);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public static void setTMXMetadata(HasTMMetadata entity, Map<String, String> metadata)
   {
      try
      {
         String metadataString = mapper.writeValueAsString(metadata);
         entity.getMetadata().put(TMMetadataType.TMX14, metadataString);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
