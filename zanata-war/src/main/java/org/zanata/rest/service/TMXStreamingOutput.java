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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import lombok.Cleanup;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.filterwriter.TMXWriter;

import org.zanata.util.NullCloseable;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

/**
 * Exports a collection of NamedDocument (ie a project iteration) to an
 * OutputStream in TMX format.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
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

      @Cleanup
      PrintWriter writer = new PrintWriter(output);
      @Cleanup
      XMLWriter xmlWriter = new XMLWriter(writer);
      @Cleanup
      MultiLangTMXWriter tmxWriter = new MultiLangTMXWriter(xmlWriter);
      tmxWriter.setWriteAllPropertiesAsAttributes(true);

      exportStrategy.exportHeader(tmxWriter);

      while (iter.hasNext())
      {
         TU tu = iter.next();
         exportStrategy.exportTranslationUnit(tmxWriter, tu);
         if (writer.checkError())
         {
            throw new IOException("error writing to output");
         }
      }
      tmxWriter.writeEndDocument();
   }

}
