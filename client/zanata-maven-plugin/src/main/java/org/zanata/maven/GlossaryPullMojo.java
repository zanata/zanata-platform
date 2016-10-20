/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.maven;

import org.zanata.client.commands.glossary.pull.GlossaryPullCommand;
import org.zanata.client.commands.glossary.pull.GlossaryPullOptions;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Pull glossary file from Zanata.
 *
 * @goal glossary-pull
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryPullMojo extends GlossaryMojo<GlossaryPullOptions>
        implements GlossaryPullOptions {

    /**
     * File type to be downloaded.
     * csv - csv file format with comma separated
     * po - a zip file of po files on available locales
     *
     * @parameter expression="${zanata.fileType}" default-value="csv"
     */
    private String fileType = "csv";

    /**
     * Optional translation languages to pull. Leave empty for all available locales
     *
     * @parameter expression="${zanata.transLang}"
     */
    private String[] transLang;

    public GlossaryPullMojo() throws Exception {
        super();
    }

    @Override
    public String getFileType() {
        return fileType;
    }

    @Override
    public GlossaryPullCommand initCommand() {
        return new GlossaryPullCommand(this);
    }

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD",
            justification = "Injected by Maven")
    @Override
    public ImmutableList<String> getTransLang() {
        if (transLang != null) {
            return ImmutableList.copyOf(transLang);
        }
        return ImmutableList.of();
    }

    @Override
    public String getCommandName() {
        return "glossary-pull";
    }
}
