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

import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.HashUtil;

/**
 * An adapter that uses a provided {@link IFilter} implementation to parse documents.
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class GenericOkapiFilterAdapter implements FileFormatAdapter
{

   /**
    * Determines how TextFlow ids are assigned for Okapi TextUnits
    */
   public enum IdSource {
      textUnitId,
      textUnitName,
      contentHash,
   };

   private final IFilter filter;
   private final IdSource idSource;

   /**
    * Create an adapter that will use filter-provided id as TextFlow id.
    * 
    * @param filter {@link IFilter} used to parse the document
    */
   public GenericOkapiFilterAdapter(IFilter filter)
   {
      this.filter = filter;
      this.idSource = IdSource.textUnitId;
   }

   /**
    * Create an adapter that will use the specified {@link IdSource} as TextFlow id.
    * 
    * @param filter {@link IFilter} used to parse the document
    * @param idSource determines how ids are assigned to TextFlows
    */
   public GenericOkapiFilterAdapter(IFilter filter, IdSource idSource)
   {
      this.filter = filter;
      this.idSource = idSource;
   }

   @Override
   public Resource parseDocumentFile(InputStream documentContent, LocaleId sourceLocale)
   {
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
   public TranslationsResource parseTranslationFile(InputStream fileContents)
   {
      TranslationsResource transRes = new TranslationsResource();
      List<TextFlowTarget> translations = transRes.getTextFlowTargets();

      // TODO look at passing the appropriate locale in to this if en is not appropriate.
      // or make sure it is processed later.
      RawDocument rawDoc = new RawDocument(fileContents, "UTF-8", net.sf.okapi.common.LocaleId.fromString("en"));
      filter.open(rawDoc);

      while (filter.hasNext()) {
         Event event = filter.next();
         if (event.getEventType() == EventType.TEXT_UNIT)
         {
            TextUnit tu = (TextUnit) event.getResource();
            if (tu.isTranslatable())
            {
               TextFlowTarget tft = new TextFlowTarget(getIdFor(tu));
               tft.setContents(tu.getSource().toString());
               tft.setState(ContentState.Approved);
               translations.add(tft);
            }
         }
      }
      filter.close();

      return transRes;
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

   /**
    * Attempt to locate a matching translation for the given id in the given list.
    * 
    * @param idToFind
    * @param translationsToSearchIn
    * @return the matching translation, or null if no translation matches the id
    */
   // TODO make targets a map against id
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

   /**
    * Return the id for a TextUnit based on id assignment rules.
    * This method can be overridden for more complex id assignment.
    * 
    * @param tu for which to get id
    * @return the id for the given tu
    */
   protected String getIdFor(TextUnit tu)
   {
      switch (idSource)
      {
      case contentHash:
         return HashUtil.generateHash(tu.getSource().toString());
      case textUnitName:
         return tu.getName();
      case textUnitId:
      default:
         return tu.getId();
      }
   }
}
