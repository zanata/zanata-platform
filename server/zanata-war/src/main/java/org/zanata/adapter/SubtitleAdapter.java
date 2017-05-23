/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.adapter;

import com.google.common.base.Charsets;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.filters.regex.RegexFilter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Adapter to handle Sub-Rip Text (.srt), WebVTT (.vtt) and SubViewer (.sbv/sub)
 * subtitle files.<br/>
 * It uses the Okapi's {@link net.sf.okapi.filters.regex.RegexFilter} class
 * with a regex specific to these formats.<br/>
 *
 * @see <a href="http://matroska.org/technical/specs/subtitles/srt.html">
 *      SubRip Text Specification</a>
 * @see <a href="http://dev.w3.org/html5/webvtt/">WebVTT Specification</a>
 *
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class SubtitleAdapter extends OkapiFilterAdapter {

    private static final String defaultConfig = loadDefaultConfig();

    private static String loadDefaultConfig() {
        URL configURL = SubtitleAdapter.class
                .getResource("SubtitleAdapterDefaultConfiguration.yml");
        try {
            return IOUtils.toString(configURL, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load default config for SRT adapter.", e);
        }
    }

    public SubtitleAdapter() {
        super(prepareFilter(), IdSource.textUnitName, true);
    }

    private static RegexFilter prepareFilter() {
        return new RegexFilter();
    }

    @Override
    protected void updateParamsWithDefaults(IParameters params) {
        params.fromString(defaultConfig);
    }
}
