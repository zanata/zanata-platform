/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.util.HashUtil;

/**
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class GenericOkapiFilterAdapter implements FileFormatAdapter
{

   private final IFilter filter;
   private final boolean useContentHashId;

   public GenericOkapiFilterAdapter(IFilter filter)
   {
      this(filter, false);
   }

   public GenericOkapiFilterAdapter(IFilter filter, boolean useContentHashId)
   {
      this.filter = filter;
      this.useContentHashId = useContentHashId;
   }

   @Override
   public Resource parseDocumentFile(InputStream documentContent, LocaleId sourceLocale)
   {
      // TODO may want to use a string locale id so it can be used both for Zanata and Okapi locale classes
      Resource document = new Resource();
      document.setLang(sourceLocale);
      document.setContentType(ContentType.TextPlain);

      List<TextFlow> resources = document.getTextFlows();

      RawDocument rawDoc = new RawDocument(documentContent, "UTF-8", net.sf.okapi.common.LocaleId.fromString("en"));
      filter.open(rawDoc);

      while (filter.hasNext()) {
         Event event = filter.next();
         if (event.getEventType() == EventType.TEXT_UNIT)
         {
            TextUnit tu = (TextUnit) event.getResource();
            if (tu.isTranslatable())
            {
               TextFlow tf = new TextFlow(getIdFor(tu), sourceLocale);
               tf.setPlural(false);
               tf.setContents(tu.getSource().toString());
               resources.add(tf);
            }
         }
      }
      filter.close();

      return document;
   }

   @Override
   public void writeTranslatedFile(OutputStream output, InputStream original, List<TextFlowTarget> translations, String locale) throws IOException
   {
      net.sf.okapi.common.LocaleId localeId = net.sf.okapi.common.LocaleId.fromString(locale);

      IFilterWriter writer = filter.createFilterWriter();
      writer.setOptions(localeId, "UTF-8");
      writer.setOutput(output);

      RawDocument rawDoc = new RawDocument(original, "UTF-8", net.sf.okapi.common.LocaleId.fromString("en"));
      filter.open(rawDoc);

      while (filter.hasNext()) {
         Event event = filter.next();
         if (event.getEventType() == EventType.TEXT_UNIT)
         {
            TextUnit tu = (TextUnit) event.getResource();
            TextFlowTarget tft = findTextFlowTarget(getIdFor(tu), translations);
            if (tft != null)
            {
               tu.setTargetContent(localeId, new TextFragment(tft.getContents().get(0)));
            }
         }
         writer.handleEvent(event);
      }
      filter.close();
      // TODO check if this is needed, or whether filter will close it.
      original.close();
      writer.close();
   }

   private TextFlowTarget findTextFlowTarget(String idToFind, List<TextFlowTarget> translationsToSearchIn)
   {
      for (TextFlowTarget target : translationsToSearchIn)
      {
         if (target.getResId().equals(idToFind))
         {
            return target;
         }
      }
      return null;
   }

   private String getIdFor(TextUnit tu)
   {
      if (useContentHashId)
      {
         return HashUtil.generateHash(tu.getSource().toString());
      }
      else
      {
         return tu.getId();
      }
   }

}
