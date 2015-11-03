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
package org.zanata.client.commands.glossary.delete;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.rest.client.GlossaryClient;
import org.zanata.rest.client.RestClientFactory;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryDeleteCommand extends
        ConfigurableCommand<GlossaryDeleteOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(GlossaryDeleteCommand.class);
    private final GlossaryClient glossaryClient;

    public GlossaryDeleteCommand(GlossaryDeleteOptions opts,
            RestClientFactory clientFactory) {
        super(opts, clientFactory);
        glossaryClient = getClientFactory().getGlossaryClient();
    }

    public GlossaryDeleteCommand(GlossaryDeleteOptions opts) {
        this(opts, OptionsUtil.createClientFactory(opts));
    }

    @Override
    public void run() throws Exception {
        log.info("Server: {}", getOpts().getUrl());
        log.info("Username: {}", getOpts().getUsername());
        log.info("Entry id to delete: {}", getOpts().getId());
        log.info("Delete entire glossary?: {}", getOpts().getAllGlossary());

        if (getOpts().getAllGlossary()) {
            glossaryClient.deleteAll();
        } else if (!StringUtils.isEmpty(getOpts().getId())) {
            glossaryClient.delete(getOpts().getId());
        } else {
            throw new RuntimeException("Option 'zanata.resId' is required.");
        }
    }
}

