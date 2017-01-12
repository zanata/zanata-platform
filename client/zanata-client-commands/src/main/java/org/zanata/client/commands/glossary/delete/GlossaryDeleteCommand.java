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


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.ConsoleInteractorImpl;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.rest.client.GlossaryClient;
import org.zanata.rest.client.RestClientFactory;

import javax.ws.rs.client.ResponseProcessingException;

import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryDeleteCommand extends
        ConfigurableCommand<GlossaryDeleteOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(GlossaryDeleteCommand.class);
    private final GlossaryClient client;

    public GlossaryDeleteCommand(GlossaryDeleteOptions opts,
            RestClientFactory clientFactory) {
        super(opts, clientFactory);
        client = getClientFactory().getGlossaryClient();
    }

    public GlossaryDeleteCommand(GlossaryDeleteOptions opts) {
        this(opts, OptionsUtil.createClientFactory(opts));
    }

    @Override
    public void run() throws Exception {
        log.info("Server: {}", getOpts().getUrl());
        log.info("Username: {}", getOpts().getUsername());
        if (!StringUtils.isEmpty(getOpts().getId())) {
            log.info("Entry id to delete: {}", getOpts().getId());
        }
        if (StringUtils.isNotBlank(getOpts().getProject())) {
            log.info("Project: {}", getOpts().getProject());
        }
        log.info("Delete entire glossary?: {}", getOpts().getAllGlossary());

        if (!getOpts().getAllGlossary() && StringUtils.isBlank(getOpts().getId())) {
            throw new RuntimeException("Option '--id' is required.");
        }

        String project = getOpts().getProject();
        String qualifiedName;
        try {
            qualifiedName = StringUtils.isBlank(project)
                    ? client.getGlobalQualifiedName()
                    : client.getProjectQualifiedName(project);
        } catch (ResponseProcessingException rpe) {
            if (rpe.getResponse().getStatus() == 404) {
                log.error("Project {} not found", project);
                return;
            } else {
                throw rpe;
            }
        }
        if (getOpts().getAllGlossary()) {
            if (getOpts().isInteractiveMode()) {
                ConsoleInteractor console = new ConsoleInteractorImpl(getOpts());
                console.printf(Question, "\nAre you sure (y/n)? ");
                console.expectYes();
            }
            Integer deletedCount = client.deleteAll(qualifiedName);
            log.info("Deleted glossary entries: {}", deletedCount);
        } else  {
            try {
                client.delete(getOpts().getId(), qualifiedName);
            } catch (ResponseProcessingException rpe) {
                if (rpe.getResponse().getStatus() == 404) {
                    log.error("Glossary entry {} not found", getOpts().getId());
                } else {
                    throw rpe;
                }
            }
        }
    }
}

