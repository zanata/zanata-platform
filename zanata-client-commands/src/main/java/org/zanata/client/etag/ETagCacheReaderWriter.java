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
package org.zanata.client.etag;

import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ETagCacheReaderWriter {
    public static ETagCache readCache(InputStream is) {
        try {
            JAXBContext jaxbCtx =
                    JAXBContext.newInstance(ETagCacheCollection.class);
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();

            ETagCacheCollection cacheCol =
                    (ETagCacheCollection) unmarshaller.unmarshal(is);
            ETagCache cache = new ETagCache(cacheCol);
            return cache;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeCache(ETagCache cache, OutputStream os) {
        try {
            JAXBContext jaxbCtx =
                    JAXBContext.newInstance(ETagCacheCollection.class);
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            marshaller.marshal(cache.asETagCacheCollection(), os);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
