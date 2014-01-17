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
package org.zanata.adapter;

import java.io.IOException;
import java.net.URL;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.filters.html.HtmlFilter;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * Adapter to handle HTML documents.
 * This adapter outputs HTML files in ASCII encoding by default to force the use
 * of CERs.
 * It uses the Okapi's {@link net.sf.okapi.filters.html.HtmlFilter} class, and
 * specifically its escapeCharacters configuration parameter to make sure all
 * HTML entities get encoded.
 */
public class HTMLAdapter extends OkapiFilterAdapter {

    private static final String defaultConfig = loadDefaultConfig();

    private static String loadDefaultConfig() {
        URL configURL =
                HTMLAdapter.class
                        .getResource("HTMLAdapterDefaultConfiguration.yml");
        try {
            return Resources.toString(configURL, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load default config for HTML adapter.", e);
        }
    }

    public HTMLAdapter() {
        super(prepareFilter(), IdSource.contentHash, true, true);
    }

    private static HtmlFilter prepareFilter() {
        return new HtmlFilter();
    }

    @Override
    protected String getOutputEncoding() {
        // Using ASCII encoding for HTML to force the output of CERs
        return "ascii";
    }

    @Override
    protected void updateParamsWithDefaults(IParameters params) {
        // IParameters has setter methods, but they break the contract in the
        // implementation for HtmlFilter and don't do anything. Have to set all
        // configuration at once rather than change individual settings.
        params.fromString(defaultConfig);
    }
}
