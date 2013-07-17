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

package org.zanata.tmx;

import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Element;

import org.zanata.model.tm.TMXMetadataHelper;
import org.zanata.model.tm.TMTranslationUnit;
import org.zanata.model.tm.TransMemory;

import com.google.common.collect.Maps;

import fj.Effect;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
class TransUnitPersister extends Effect<Element>
{
   private final TransMemory tm;

   TransUnitPersister(TransMemory tm)
   {
      this.tm = tm;
   }

   @Override
   public void e(Element tuElem)
   {
      TMTranslationUnit tu = new TMTranslationUnit();
      tu.setTranslationMemory(tm);

      Map<String, String> metadata = Maps.newHashMap();
      for (int i=0; i < tuElem.getAttributeCount(); i++)
      {
         Attribute attr = tuElem.getAttribute(i);
         String name = attr.getQualifiedName();
         String value = attr.getValue();
         metadata.put(name, value);
      }
      TMXMetadataHelper.setMetadata(tu, metadata);

//          TMTransUnitVariant tuv = handleTransUnitVariant(nextEvent);
//          tu.getTransUnitVariants().put(tuv.getLanguage(), tuv);
//            tu.setTransUnitVariants(transUnitVariants);
      tu.setVersionNum(0);
      // TODO Save the TU
      // TMTranslationUnitDAO.makePersistent(tu);
   }

}
