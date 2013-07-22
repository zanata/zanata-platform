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

package org.zanata.rest.service;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import lombok.Cleanup;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.filterwriter.TMXWriter;

import org.zanata.common.LocaleId;
import org.zanata.model.SourceContents;
import org.zanata.util.NullCloseable;
import org.zanata.util.OkapiUtil;
import org.zanata.util.VersionUtility;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

/**
 * Exports a collection of NamedDocument (ie a project iteration) to an
 * OutputStream in TMX format.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TMXStreamingOutput implements StreamingOutput
{
   private static final String creationTool = "Zanata " + TMXStreamingOutput.class.getSimpleName();
   private static final String creationToolVersion =
         VersionUtility.getVersionInfo(TMXStreamingOutput.class).getVersionNo();
   private final @Nonnull Iterator<? extends SourceContents> tuIter;
   private final ExportTUStrategy exportTUStrategy;

   public TMXStreamingOutput(@Nonnull Iterator<? extends SourceContents> tuIter,
         @Nullable LocaleId targetLocale)
   {
      this.tuIter = tuIter;
      this.exportTUStrategy = new ExportTUStrategy(targetLocale);
   }

   @Override
   public void write(OutputStream output) throws IOException, WebApplicationException
   {
      @Cleanup
      Closeable closeable = (Closeable) (tuIter instanceof Closeable ? tuIter : NullCloseable.INSTANCE);
      @SuppressWarnings("null")
      PeekingIterator<SourceContents> iter = Iterators.peekingIterator(tuIter);
      // Fetch the first result, so that we can fail fast, before
      // writing any output. This should enable RESTEasy to return an
      // error instead of simply aborting the output stream.
      if (iter.hasNext()) iter.peek();

      @Cleanup
      PrintWriter writer = new PrintWriter(output);
      @Cleanup
      XMLWriter xmlWriter = new XMLWriter(writer);
      @Cleanup
      TMXWriter tmxWriter = new TMXWriter(xmlWriter);
      String segType = "block"; // TODO other segmentation types
      String dataType = "unknown"; // TODO track data type metadata throughout the system

      net.sf.okapi.common.LocaleId allLocale = new net.sf.okapi.common.LocaleId("*all*", false);

      tmxWriter.writeStartDocument(
            allLocale,
            // TMXWriter demands a non-null target locale, but if you write
            // your TUs with writeTUFull(), it is never used.
            net.sf.okapi.common.LocaleId.EMPTY,
            creationTool, creationToolVersion,
            segType, null, dataType);

      while (iter.hasNext())
      {
         SourceContents tu = iter.next();
         net.sf.okapi.common.LocaleId sourceLocale = OkapiUtil.toOkapiLocale(tu.getLocale());
         exportTUStrategy.exportTranslationUnit(tmxWriter, tu, sourceLocale);
         if (writer.checkError())
         {
            throw new IOException("error writing to output");
         }
      }
      tmxWriter.writeEndDocument();
   }

}
