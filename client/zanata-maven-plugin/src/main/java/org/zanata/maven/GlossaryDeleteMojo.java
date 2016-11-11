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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.glossary.delete.GlossaryDeleteCommand;
import org.zanata.client.commands.glossary.delete.GlossaryDeleteOptions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Delete glossary entry from Zanata.
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Mojo(name = "glossary-delete", requiresOnline = true, requiresProject = false)
public class GlossaryDeleteMojo extends GlossaryMojo<GlossaryDeleteOptions>
        implements GlossaryDeleteOptions {

    /**
     * id of glossary to delete
     */
    @Parameter(property = "zanata.id")
    private String id;

    /**
     * Delete *all* glossaries
     */
    @Parameter(property = "zanata.allGlossary", defaultValue = "false")
    private boolean allGlossary = false;

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD",
            justification = "Injected by Maven")
    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean getAllGlossary() {
        return allGlossary;
    }

    @Override
    public GlossaryDeleteCommand initCommand() {
        return new GlossaryDeleteCommand(this);
    }

    @Override
    public String getCommandName() {
        return "glossary-delete";
    }
}
