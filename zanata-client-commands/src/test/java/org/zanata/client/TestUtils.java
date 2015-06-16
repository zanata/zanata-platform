/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

package org.zanata.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.config.LocaleMapping;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TestUtils {
    /**
     * Creates a LocaleMapping with optional map from and then add to options.
     * @return the created LocaleMapping
     */
    public static LocaleMapping createAndAddLocaleMapping(String localeId,
            Optional<String> optionalMapFrom, ConfigurableProjectOptions opts) {
        LocaleMapping mapping = new LocaleMapping(localeId);
        if (optionalMapFrom.isPresent()) {
            mapping.setMapFrom(optionalMapFrom.get());
        }
        opts.getLocaleMapList().add(mapping);
        return mapping;
    }

    public static String readFromClasspath(String relativePath)
            throws IOException {
        URL resource = loadFromClasspath(relativePath);
        return IOUtils.toString(resource, Charsets.UTF_8);
    }

    public static File fileFromClasspath(String relativePath) {
        URL url = loadFromClasspath(relativePath);
        return new File(url.getFile());
    }

    private static URL loadFromClasspath(String relativePath) {
        URL resource =
                Thread.currentThread().getContextClassLoader()
                        .getResource(relativePath);
        Preconditions.checkArgument(resource != null, "%s not found",
                relativePath);
        return resource;
    }
}
