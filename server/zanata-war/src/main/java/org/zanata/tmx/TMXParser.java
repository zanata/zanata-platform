/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.tmx;

import java.io.InputStream;
import javax.enterprise.context.Dependent;
import javax.persistence.EntityExistsException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import nu.xom.Element;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.util.ElementBuilder;
import org.zanata.model.tm.TransMemory;
import org.zanata.transaction.TransactionUtilImpl;
import org.zanata.util.RunnableEx;
import org.zanata.util.TMXParseException;
import org.zanata.xml.TmxDtdResolver;
import com.google.common.base.Throwables;

/**
 * Parses TMX input.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("tmxParser")
@Dependent
public class TMXParser {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TMXParser.class);

    // Batch size to commit in a new transaction for long files
    private static final int BATCH_SIZE = 100;
    @Inject
    private Session session;
    @Inject
    private TransMemoryAdapter transMemoryAdapter;

    public void parseAndSaveTMX(InputStream input, TransMemory transMemory)
            throws TMXParseException, SecurityException, IllegalStateException,
            RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SystemException, NotSupportedException {
        int handledTUs = 0;
        try {
            log.info("parsing started for: {}", transMemory.getSlug());
            session.setFlushMode(FlushMode.MANUAL);
            session.setCacheMode(CacheMode.IGNORE);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
            factory.setProperty(XMLInputFactory.IS_VALIDATING, true);
            factory.setXMLResolver(new TmxDtdResolver());
            XMLStreamReader reader = factory.createXMLStreamReader(input);
            try {
                QName tmx = new QName("tmx");
                while (reader.hasNext()
                        && reader.next() != XMLStreamConstants.START_ELEMENT) {
                }
                if (!reader.hasNext())
                    throw new TMXParseException("No root element");
                if (!reader.getName().equals(tmx))
                    throw new TMXParseException(
                            "Wrong root element: expected tmx");
                // At this point, event = START_ELEMENT and name = tmx
                while (reader.hasNext()) {
                    CommitBatch commitBatch =
                            new CommitBatch(reader, 0, transMemory);
                    TransactionUtilImpl.get().runEx(commitBatch);
                    handledTUs += commitBatch.handledTUs;
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (EntityExistsException e) {
            String msg =
                    "Possible duplicate TU (duplicate tuid or duplicatesrc content without tuid)";
            throw new TMXParseException(msg, e);
        } catch (Exception e) {
            Throwable rootCause = Throwables.getRootCause(e);
            if (rootCause instanceof TMXParseException) {
                throw (TMXParseException) e;
            } else if (rootCause instanceof XMLStreamException) {
                throw new TMXParseException(rootCause);
            } else {
                throw Throwables.propagate(e);
            }
        } finally {
            log.info("parsing stopped for: {}, TU count={}",
                    transMemory.getSlug(), handledTUs);
        }
    }

    private class CommitBatch implements RunnableEx {
        private XMLStreamReader reader;
        private int handledTUs;
        private TransMemory transMemory;
        private final QName header = new QName("header");
        private final QName tu = new QName("tu");

        @Override
        public void run() throws Exception {
            while (reader.hasNext() && handledTUs < BATCH_SIZE) {
                int eventType = reader.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    QName elemName = reader.getName();
                    if (elemName.equals(tu)) {
                        Element tuElem = ElementBuilder.buildElement(reader);
                        transMemoryAdapter.processTransUnit(transMemory,
                                tuElem);
                        handledTUs++;
                    } else if (elemName.equals(header)) {
                        Element headerElem =
                                ElementBuilder.buildElement(reader);
                        transMemoryAdapter.processHeader(transMemory,
                                headerElem);
                    }
                }
            }
        }

        @java.beans.ConstructorProperties({ "reader", "handledTUs",
                "transMemory" })
        public CommitBatch(final XMLStreamReader reader, final int handledTUs,
                final TransMemory transMemory) {
            this.reader = reader;
            this.handledTUs = handledTUs;
            this.transMemory = transMemory;
        }
    }
}
