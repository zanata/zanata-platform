/*
 * Copyright 2010, 2017, Red Hat, Inc. and individual contributors as indicated by the
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

import java.io.StringReader;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class TMXUtil {

    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

    static {
        getXmlInputFactory().setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    private TMXUtil() {
    }

    private static XMLInputFactory getXmlInputFactory() {
        return xmlInputFactory;
    }

    /**
     * Extracts plain text from a TMX entry. This ignores the TMX elements that
     * mark up native code sequences: {@code
     * <bpt></bpt>
     * <ept></ept>
     * <it></it>
     * <ph></ph>
     * <seg></seg>}
     *
     * @param content
     *            The tmx marked up content.
     * @return A string with all tmx mark-up content stripped out. Essentially a
     *         plain text version of the string.
     */
    public static String removeFormattingMarkup(String content) {
        // The content must be a fully formed <seg> element (attributes
        // optional). Leading or trailing whitespace will be ignored.
        try {
            XMLEventReader reader = getXmlInputFactory().createXMLEventReader(
                    new StringReader(content));
            try {
                StringBuilder writer = new StringBuilder();

                // Nesting level. When this is > 0 it means we are ignoring events
                int ignoreLevel = 0;

                while (reader.hasNext()) {
                    XMLEvent nextEv = reader.nextEvent();

                    switch (nextEv.getEventType()) {
                        case START_ELEMENT:
                            ignoreLevel =
                                    handleStartElem(ignoreLevel,
                                            nextEv.asStartElement());
                            break;
                        case END_ELEMENT:
                            ignoreLevel =
                                    handleEndElem(ignoreLevel,
                                            nextEv.asEndElement());
                            break;
                        case CHARACTERS:
                        case CDATA:
                            if (ignoreLevel == 0)
                                writer.append(nextEv.asCharacters().getData());
                            break;
                        default:
                            // Ignore uninteresting types
                    }
                }
                return writer.toString();
            } finally {
                reader.close();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private static int handleStartElem(int ignoreLevel, StartElement startElem) {
        String elemName = startElem.getName().getLocalPart();
        if (ignoreElement(elemName)) {
            return ignoreLevel + 1;
        }
        return ignoreLevel;
    }

    private static int handleEndElem(int ignoreLevel, EndElement endElem) {
        String elemName = endElem.getName().getLocalPart();

        if (ignoreElement(elemName)) {
            if (ignoreLevel > 0) {
                return ignoreLevel - 1;
            }
        }
        return ignoreLevel;
    }

    private static boolean ignoreElement(String elemName) {
        // NB we do want the contents of 'hi' elements, but not these elements:
        return elemName.equals("bpt") || elemName.equals("ept")
                || elemName.equals("it") || elemName.equals("ph")
                || elemName.equals("sub");
    }

}
