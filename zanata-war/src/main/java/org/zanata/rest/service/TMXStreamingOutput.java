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
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Text;

import org.zanata.common.LocaleId;
import org.zanata.util.NullCloseable;
import org.zanata.util.TMXUtils;
import org.zanata.xml.StreamSerializer;

import com.google.common.base.Optional;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

/**
 * Exports a collection of NamedDocument (ie a project iteration) to an
 * OutputStream in TMX format.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Slf4j
public class TMXStreamingOutput<TU> implements StreamingOutput
{
   private final @Nonnull Iterator<TU> tuIter;
   private final ExportTMXStrategy<TU> exportStrategy;

   public TMXStreamingOutput(
         @Nonnull Iterator<TU> tuIter,
         @Nonnull ExportTMXStrategy<TU> exportTUStrategy)
   {
      this.tuIter = tuIter;
      this.exportStrategy = exportTUStrategy;
   }

   @Override
   public void write(OutputStream output) throws IOException, WebApplicationException
   {
      @Cleanup
      Closeable closeable = (Closeable) (tuIter instanceof Closeable ? tuIter : NullCloseable.INSTANCE);
      @SuppressWarnings("null")
      PeekingIterator<TU> iter = Iterators.peekingIterator(tuIter);
      // Fetch the first result, so that we can fail fast, before
      // writing any output. This should enable RESTEasy to return an
      // error instead of simply aborting the output stream.
      if (iter.hasNext()) iter.peek();

      StreamSerializer tmxWriter = new StreamSerializer(output);
      // Can't use Serializer's indent, or it will mess up whitespace in seg elements
//      tmxWriter.setIndent(2);

      Element tmx = new Element("tmx");
      tmx.addAttribute(new Attribute("version", "1.4"));
      tmxWriter.writeStartTag(tmx);
      tmxWriter.newLine();

      Text indentText = new Text("  ");
      tmxWriter.write(indentText);
      Element header = exportStrategy.buildHeader();
      tmxWriter.write(header);
      tmxWriter.newLine();

      tmxWriter.write(indentText);
      Element body = new Element("body");
      tmxWriter.writeStartTag(body);
      tmxWriter.newLine();

      while (iter.hasNext())
      {
         TU tu = iter.next();
         Element textUnit = exportStrategy.buildTU(tu);
         // If there aren't any translations for this TU, we shouldn't include it.
         // From the TMX spec: "Logically, a complete translation-memory
         // database will contain at least two <tuv> elements in each translation
         // unit."
         if (textUnit != null && textUnit.getChildElements("tuv").size() >= 2)
         {
            tmxWriter.write(textUnit);
            tmxWriter.newLine();
         }
      }
      tmxWriter.write(indentText);
      tmxWriter.writeEndTag(body);
      tmxWriter.newLine();
      tmxWriter.writeEndTag(tmx);
      tmxWriter.newLine();
      tmxWriter.flush();
   }

}
