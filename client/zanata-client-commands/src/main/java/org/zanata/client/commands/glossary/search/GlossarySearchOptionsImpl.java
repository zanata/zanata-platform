/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.client.commands.glossary.search;

import org.kohsuke.args4j.Option;
import org.zanata.client.commands.ConfigurableGlossaryOptionsImpl;
import org.zanata.client.commands.ZanataCommand;

public class GlossarySearchOptionsImpl extends ConfigurableGlossaryOptionsImpl
        implements GlossarySearchOptions {

    private String filter;
    private boolean raw;

    @Override
    public String getFilter() {
        return filter;
    }

    @Override
    public boolean getRaw() {
        return this.raw;
    }

    @Option(name = "--filter", metaVar = "FILTER", required = true,
            usage = "Search term for glossary entries.")
    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Option(name = "--raw", metaVar = "RAW",
            usage = "Print the raw JSON response.")
    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    @Override
    public ZanataCommand initCommand() {
        return new GlossarySearchCommand(this);
    }

    @Override
    public String getCommandName() {
        return "glossary-search";
    }

    @Override
    public String getCommandDescription() {
        return "Find glossary entries in Zanata";
    }
}
