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
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Text;

import org.zanata.util.CloseableIterator;
import org.zanata.util.NullCloseable;
import org.zanata.xml.StreamSerializer;

import com.google.common.base.Optional;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

/**
 * Exports a series of tranlation units (T) to an OutputStream in TMX format.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @param T
 */
@ParametersAreNonnullByDefault
public class TMXStreamingOutput<T> implements StreamingOutput, Closeable
{
   private final @Nonnull Iterator<T> tuIter;
   private final TMXExportStrategy<T> exportStrategy;
   private final Closeable closeable;

   private TMXStreamingOutput(
         @Nonnull Iterator<T> tuIter,
         @Nonnull TMXExportStrategy<T> exportTUStrategy,
         Closeable closeable)
   {
      this.tuIter = tuIter;
      this.exportStrategy = exportTUStrategy;
      this.closeable = (Closeable) (tuIter instanceof Closeable ? tuIter : NullCloseable.INSTANCE);
   }

   /**
    * Constructs an instance which will write the translation units using the
    * specified export strategy.
    * @param tuIter an iterator over translation units to be exported.
    * It will be closed after write() is called, or call close() to
    * close it earlier.
    * @param exportTUStrategy strategy to use when converting from translation units into TMX.
    */
   public TMXStreamingOutput(
         @Nonnull CloseableIterator<T> tuIter,
         @Nonnull TMXExportStrategy<T> exportTUStrategy)
   {
      this(tuIter, exportTUStrategy, tuIter);
   }

   /**
    * Constructs an instance which will write the translation units using the
    * specified export strategy.
    * <p>
    * Note that this constructor DOES NOT guarantee close the iterator.
    * If you want the iterator closed, you should pass a CloseableIterator.
    * @param tuIter an iterator over translation units to be exported.  It will NOT be closed.
    * @param exportTUStrategy strategy to use when converting from translation units into TMX.
    */
   public TMXStreamingOutput(
         @Nonnull Iterator<T> tuIter,
         @Nonnull TMXExportStrategy<T> exportTUStrategy)
   {
      this(tuIter, exportTUStrategy, NullCloseable.INSTANCE);
   }

   @Override
   public void close() throws IOException
   {
      closeable.close();
   }

   /**
    * Goes through the translation units returned by this object's iterator
    * (see {@link #TMXStreamingOutput(Iterator, TMXExportStrategy)}
    * and writes each one to the OutputStream in TMX form.
    * <p>
    * Any resources associated with the iterator will be closed before
    * this method exits.
    */
   @Override
   public void write(OutputStream output) throws IOException, WebApplicationException
   {
      try
      {
         @SuppressWarnings("null")
         PeekingIterator<T> iter = Iterators.peekingIterator(tuIter);
         // Fetch the first result, so that we can fail fast, before
         // writing any output. This should enable RESTEasy to return an
         // error instead of simply aborting the output stream.
         if (iter.hasNext()) iter.peek();

         StreamSerializer tmxWriter = new StreamSerializer(output);
         tmxWriter.writeXMLDeclaration();

         Element tmx = new Element("tmx");
         tmx.addAttribute(new Attribute("version", "1.4"));
         tmxWriter.writeStartTag(tmx);
         tmxWriter.writeNewLine();

         Text indentText = new Text("  ");
         tmxWriter.write(indentText);
         Element header = exportStrategy.buildHeader();
         tmxWriter.write(header);
         tmxWriter.writeNewLine();

         tmxWriter.write(indentText);
         Element body = new Element("body");
         tmxWriter.writeStartTag(body);
         tmxWriter.writeNewLine();

         while (iter.hasNext())
         {
            T tu = iter.next();
            writeIfComplete(tmxWriter, tu);
         }
         tmxWriter.write(indentText);
         tmxWriter.writeEndTag(body);
         tmxWriter.writeNewLine();
         tmxWriter.writeEndTag(tmx);
         tmxWriter.writeNewLine();
         tmxWriter.flush();
      }
      finally
      {
         close();
      }
   }

   private void writeIfComplete(StreamSerializer tmxWriter, T tu) throws IOException
   {
      Optional<Element> textUnit = exportStrategy.buildTU(tu);
      // If there aren't any translations for this TU, we shouldn't include it.
      // From the TMX spec: "Logically, a complete translation-memory
      // database will contain at least two <tuv> elements in each translation
      // unit."
      if (textUnit.isPresent() && textUnit.get().getChildElements("tuv").size() >= 2)
      {
         tmxWriter.write(textUnit.get());
         tmxWriter.writeNewLine();
      }
   }

}
