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

import java.io.File;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.glossary.push.GlossaryPushCommand;
import org.zanata.client.commands.glossary.push.GlossaryPushOptions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Pushes glossary file into Zanata.
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Mojo(name = "glossary-push", requiresOnline = true, requiresProject = false)
public class GlossaryPushMojo extends GlossaryMojo<GlossaryPushOptions>
        implements GlossaryPushOptions {

    /**
     * Translation language of document. Not required for csv file
     */
    @Parameter(property = "zanata.transLang")
    private String transLang;

    /**
     * Location path for the glossary file
     */
    @Parameter(property = "zanata.file", required = true)
    private File file;

    /**
     * Batch size for large glossary file
     */
    @Parameter(property = "zanata.batchSize", defaultValue = "50")
    private int batchSize = DEFAULT_BATCH_SIZE;

    public GlossaryPushMojo() throws Exception {
        super();
    }

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD",
            justification = "Injected by Maven")
    @Override
    public File getFile() {
        return file;
    }

    @Override
    public GlossaryPushCommand initCommand() {
        return new GlossaryPushCommand(this);
    }

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD",
            justification = "Injected by Maven")
    @Override
    public String getTransLang() {
        return transLang;
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public String getCommandName() {
        return "glossary-push";
    }
}
