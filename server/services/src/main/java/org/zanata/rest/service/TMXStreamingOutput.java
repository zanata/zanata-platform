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
import nu.xom.DocType;
import nu.xom.Element;
import nu.xom.Text;
import org.zanata.util.CloseableIterator;
import org.zanata.util.NullCloseable;
import org.zanata.xml.StreamSerializer;
import com.google.common.base.Optional;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

/**
 * Exports a series of translation units (T) to an OutputStream in TMX format.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @param T
 */
@ParametersAreNonnullByDefault
public class TMXStreamingOutput<T> implements StreamingOutput, Closeable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TMXStreamingOutput.class);
    @Nonnull
    private final Iterator<T> tuIter;
    private final TMXExportStrategy<T> exportStrategy;
    private final Closeable closeable;
    private final String jobName;

    private TMXStreamingOutput(String jobName, Iterator<T> tuIter,
            TMXExportStrategy<T> exportTUStrategy, Closeable closeable) {
        this.jobName = jobName;
        this.tuIter = tuIter;
        this.exportStrategy = exportTUStrategy;
        this.closeable = (Closeable) (tuIter instanceof Closeable ? tuIter
                : NullCloseable.INSTANCE);
    }

    /**
     * Constructs an instance which will write the translation units using the
     * specified export strategy.
     *
     * @param tuIter
     *            an iterator over translation units to be exported. It will be
     *            closed after write() is called, or call close() to close it
     *            earlier.
     * @param exportTUStrategy
     *            strategy to use when converting from translation units into
     *            TMX.
     */
    public TMXStreamingOutput(String jobName, CloseableIterator<T> tuIter,
            TMXExportStrategy<T> exportTUStrategy) {
        this(jobName, tuIter, exportTUStrategy, tuIter);
    }

    /**
     * Constructs an instance which will write the translation units using the
     * specified export strategy.
     * <p>
     * Note that this constructor DOES NOT guarantee close the iterator. If you
     * want the iterator closed, you should pass a CloseableIterator.
     *
     * @param tuIter
     *            an iterator over translation units to be exported. It will NOT
     *            be closed.
     * @param exportTUStrategy
     *            strategy to use when converting from translation units into
     *            TMX.
     */
    public static <T> TMXStreamingOutput<T> testInstance(Iterator<T> tuIter,
            TMXExportStrategy<T> exportTUStrategy) {
        return new TMXStreamingOutput<T>("test", tuIter, exportTUStrategy,
                NullCloseable.INSTANCE);
    }

    @Override
    public void close() throws IOException {
        closeable.close();
    }

    /**
     * Goes through the translation units returned by this object's iterator
     * (see {@link #TMXStreamingOutput(Iterator, TMXExportStrategy)} and writes
     * each one to the OutputStream in TMX form.
     * <p>
     * Any resources associated with the iterator will be closed before this
     * method exits.
     */
    @Override
    public void write(OutputStream output)
            throws IOException, WebApplicationException {
        int tuCount = 0;
        try {
            log.info("streaming output started for: {}", jobName);
            @SuppressWarnings("null")
            PeekingIterator<T> iter = Iterators.peekingIterator(tuIter);
            // Fetch the first result, so that we can fail fast, before
            // writing any output. This should enable RESTEasy to return an
            // error instead of simply aborting the output stream.
            if (iter.hasNext())
                iter.peek();
            StreamSerializer stream = new StreamSerializer(output);
            stream.writeXMLDeclaration();
            stream.write(
                    new DocType("tmx", "http://www.lisa.org/tmx/tmx14.dtd"));
            stream.writeNewLine();
            Element tmx = new Element("tmx");
            tmx.addAttribute(new Attribute("version", "1.4"));
            startElem(stream, tmx);
            indent(stream);
            writeElem(stream, exportStrategy.buildHeader());
            indent(stream);
            Element body = new Element("body");
            startElem(stream, body);
            while (iter.hasNext()) {
                T tu = iter.next();
                writeIfComplete(stream, tu);
                ++tuCount;
            }
            indent(stream);
            endElem(stream, body);
            endElem(stream, tmx);
            stream.flush();
        } finally {
            close();
            log.info("streaming output stopped for: {}, TU count={}", jobName,
                    tuCount);
        }
    }

    private void indent(StreamSerializer stream) throws IOException {
        stream.write(new Text("  "));
    }

    private void startElem(StreamSerializer stream, Element elem)
            throws IOException {
        stream.writeStartTag(elem);
        stream.writeNewLine();
    }

    private void endElem(StreamSerializer stream, Element elem)
            throws IOException {
        stream.writeEndTag(elem);
        stream.writeNewLine();
    }

    private void writeElem(StreamSerializer stream, Element elem)
            throws IOException {
        stream.write(elem);
        stream.writeNewLine();
    }

    private void writeIfComplete(StreamSerializer stream, T tu)
            throws IOException {
        Optional<Element> textUnit = exportStrategy.buildTU(tu);
        // If there aren't any translations for this TU, we shouldn't include
        // it.
        // From the TMX spec: "Logically, a complete translation-memory
        // database will contain at least two <tuv> elements in each translation
        // unit."
        if (textUnit.isPresent()
                && textUnit.get().getChildElements("tuv").size() >= 2) {
            writeElem(stream, textUnit.get());
        }
    }
}
