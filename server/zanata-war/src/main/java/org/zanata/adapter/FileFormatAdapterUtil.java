/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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

import org.zanata.exception.FileFormatAdapterException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class FileFormatAdapterUtil {
    private FileFormatAdapterUtil() {
    }

    static BufferedInputStream readStream(URI fileUri)
            throws FileFormatAdapterException,
            IllegalArgumentException {
        URL url = null;

        try {
            url = fileUri.toURL();
            return new BufferedInputStream(url.openStream());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Could not open the URI. The URI must be absolute: "
                            + ((url == null) ? "URL is null" : url.toString()),
                    e);
        } catch (MalformedURLException e) {
            throw new FileFormatAdapterException(
                    "Could not open the URI. The URI may be malformed: "
                            + ((url == null) ? "URL is null" : url.toString()),
                    e);
        } catch (IOException e) {
            throw new FileFormatAdapterException(
                    "Could not open the URL. The URL is OK but the input stream could not be opened.\n"
                            + e.getMessage(), e);
        }
    }
}
