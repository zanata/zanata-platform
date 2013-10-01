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

package org.zanata.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TmxDtdResolver implements XMLResolver, EntityResolver {
    // example system IDs:
    // http://www.lisa.org/tmx/tmx14.dtd
    // http://www.ttt.org/oscarstandards/tmx/tmx14.dtd
    private static boolean isTMX14(String systemId) {
        return systemId.endsWith("tmx14.dtd");
    }

    @Override
    public Object resolveEntity(String publicID, String systemID,
            String baseURI, String namespace) throws XMLStreamException {
        if (isTMX14(systemID)) {
            InputStream stream = getClass().getResourceAsStream("tmx14.dtd");
            return stream;
        } else {
            throw new XMLStreamException(
                    "Invalid TMX document: expected tmx14.dtd");
        }
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        if (isTMX14(systemId)) {
            InputStream stream = getClass().getResourceAsStream("tmx14.dtd");
            return new InputSource(stream);
        } else {
            throw new SAXException("Invalid TMX document: expected tmx14.dtd");
        }
    }

}
