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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.exception.FileFormatAdapterException;
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
   private boolean requireUriRawDoc;
   private boolean requireFileOutput;

   /**
    * Create an adapter that will use filter-provided id as TextFlow id.
    * 
    * @param filter {@link IFilter} used to parse the document
    */
   public GenericOkapiFilterAdapter(IFilter filter)
   {
      this(filter, IdSource.textUnitId);
   }

   /**
    * Create an adapter that will use the specified {@link IdSource} as TextFlow id.
    * 
    * @param filter {@link IFilter} used to parse the document
    * @param idSource determines how ids are assigned to TextFlows
    */
   public GenericOkapiFilterAdapter(IFilter filter, IdSource idSource)
   {
      this(filter, idSource, false, false);
   }

   /**
    * Create an adapter that will use the specified {@link IdSource} as TextFlow id.
    * 
    * @param filter {@link IFilter} used to parse the document
    * @param idSource determines how ids are assigned to TextFlows
    */
   public GenericOkapiFilterAdapter(IFilter filter, IdSource idSource, boolean requireUriRawDoc, boolean requireFileOutput)
   {
      this.filter = filter;
      this.idSource = idSource;
      this.requireUriRawDoc = requireUriRawDoc;
      this.requireFileOutput = requireFileOutput;
   }

   @Override
   public Resource parseDocumentFile(URI documentContent, LocaleId sourceLocale) throws FileFormatAdapterException, IllegalArgumentException
   {
      // null documentContent is handled by RawDocument constructor
      if (sourceLocale == null)
      {
         throw new IllegalArgumentException("Source locale cannot be null");
      }

      Resource document = new Resource();
      document.setLang(sourceLocale);
      document.setContentType(ContentType.TextPlain);

      List<TextFlow> resources = document.getTextFlows();

      RawDocument rawDoc = new RawDocument(documentContent, "UTF-8", net.sf.okapi.common.LocaleId.fromString("en"));

      try
      {
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
      }
      catch (OkapiIOException e)
      {
         throw new FileFormatAdapterException("Unable to parse document", e);
      }
      finally
      {
         filter.close();
      }
      return document;
   }

   @Override
   public TranslationsResource parseTranslationFile(InputStream fileContents, String localeId) throws FileFormatAdapterException, IllegalArgumentException
   {
      if (fileContents == null)
      {
         throw new IllegalArgumentException("File contents cannot be null");
      }
      if (localeId == null || localeId.isEmpty())
      {
         throw new IllegalArgumentException("locale id string cannot be null or empty");
      }

      if (requireUriRawDoc)
      {
         return parseTranslationFileWithUrlRawDoc(fileContents);
      }
      else
      {
         RawDocument rawDoc = new RawDocument(fileContents, "UTF-8", net.sf.okapi.common.LocaleId.fromString(localeId));
         return parseTranslationFile(rawDoc);
      }
   }

   private TranslationsResource parseTranslationFile(RawDocument rawDoc)
   {
      TranslationsResource transRes = new TranslationsResource();
      List<TextFlowTarget> translations = transRes.getTextFlowTargets();

      try
      {
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
      }
      catch (OkapiIOException e)
      {
         throw new FileFormatAdapterException("Unable to parse translation file", e);
      }
      finally
      {
         filter.close();
      }
      return transRes;
   }

   private TranslationsResource parseTranslationFileWithUrlRawDoc(InputStream fileContents)
   {
      File tempFile = null;
      try
      {
         tempFile = File.createTempFile("filename", "extension");

         byte[] buffer = new byte[4096]; // To hold file contents
         int bytesRead;
         FileOutputStream output;

         output = new FileOutputStream(tempFile);
         while ((bytesRead = fileContents.read(buffer)) != -1)
         {
            output.write(buffer, 0, bytesRead);
         }
         output.close();
      }
      catch (IOException e)
      {
         throw new FileFormatAdapterException("Error while writing translation file to temporary location", e);
      }

      RawDocument rawDoc = new RawDocument(tempFile.toURI(), "UTF-8", net.sf.okapi.common.LocaleId.fromString("en"));
      TranslationsResource transRes = parseTranslationFile(rawDoc);

      if (tempFile != null)
      {
         if (!tempFile.delete())
         {
            // TODO log warning "unable to remove temporary file {}, marked for delete on exit"
            tempFile.deleteOnExit();
         }
      }
      return transRes;
   }


   @Override
   public void writeTranslatedFile(OutputStream output, URI originalFile, Map<String, TextFlowTarget> translations, String locale)
   {
      net.sf.okapi.common.LocaleId localeId = net.sf.okapi.common.LocaleId.fromString(locale);
      IFilterWriter writer = filter.createFilterWriter();
      writer.setOptions(localeId, "UTF-8");

      if (requireFileOutput)
      {
         writeTranslatedFileWithFileOutput(output, originalFile, translations, localeId, writer);
      }
      else
      {
         writer.setOutput(output);
         generateTranslatedFile(originalFile, translations, localeId, writer);
      }
   }

   private void writeTranslatedFileWithFileOutput(OutputStream output, URI originalFile, Map<String, TextFlowTarget> translations, net.sf.okapi.common.LocaleId localeId, IFilterWriter writer)
   {
      File tempFile = null;

      try
      {
         tempFile = File.createTempFile("filename", "extension");
         writer.setOutput(tempFile.getCanonicalPath());
         generateTranslatedFile(originalFile, translations, localeId, writer);

         byte[] buffer = new byte[4096]; // To hold file contents
         int bytesRead;
         FileInputStream input = new FileInputStream(tempFile);
         while ((bytesRead = input.read(buffer)) != -1)
         {
            output.write(buffer, 0, bytesRead);
         }
      }
      catch (IOException e)
      {
         // FIXME log
         throw new FileFormatAdapterException("Unable to generate translated file", e);
      }
      catch (SecurityException e)
      {
         // FIXME log
         throw new FileFormatAdapterException("Unable to generate translated file", e);
      }
      finally
      {
         if (tempFile != null)
         {
            if (!tempFile.delete())
            {
               // TODO log warning "unable to remove temporary file {}, marked for delete on exit"
               tempFile.deleteOnExit();
            }
         }
      }

   }

   private void generateTranslatedFile(URI originalFile, Map<String, TextFlowTarget> translations, net.sf.okapi.common.LocaleId localeId, IFilterWriter writer)
   {
      RawDocument rawDoc = new RawDocument(originalFile, "UTF-8", net.sf.okapi.common.LocaleId.fromString("en"));

      try
      {
         filter.open(rawDoc);
         while (filter.hasNext()) {
            Event event = filter.next();
            if (event.getEventType() == EventType.TEXT_UNIT)
            {
               TextUnit tu = (TextUnit) event.getResource();
               TextFlowTarget tft = translations.get(getIdFor(tu));
               if (tft != null)
               {
                  tu.setTargetContent(localeId, new TextFragment(tft.getContents().get(0)));
               }
            }
            writer.handleEvent(event);
         }
      }
      catch (OkapiIOException e)
      {
         throw new FileFormatAdapterException("Unable to generate translated document from original", e);
      }
      finally
      {
         filter.close();
         writer.close();
      }
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
