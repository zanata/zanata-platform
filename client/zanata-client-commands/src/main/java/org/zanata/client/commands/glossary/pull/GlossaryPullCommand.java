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
package org.zanata.client.commands.glossary.pull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.rest.client.ClientUtil;
import org.zanata.rest.client.GlossaryClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.util.PathUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryPullCommand extends
        ConfigurableCommand<GlossaryPullOptions> {
    private static final Logger log =
            LoggerFactory.getLogger(GlossaryPullCommand.class);

    private final GlossaryClient client;

    public GlossaryPullCommand(GlossaryPullOptions opts,
            RestClientFactory clientFactory) {
        super(opts, clientFactory);
        client = getClientFactory().getGlossaryClient();
    }

    public GlossaryPullCommand(GlossaryPullOptions opts) {
        this(opts, OptionsUtil.createClientFactory(opts));
    }

    @Override
    public void run() throws Exception {
        String fileType = StringUtils.isEmpty(getOpts().getFileType()) ? "csv"
                : getOpts().getFileType();
        if (!fileType.equalsIgnoreCase("po")
                && !fileType.equalsIgnoreCase("csv")) {
            throw new RuntimeException(
                    "Option '--file-type' is not valid. Please use 'csv' or 'po'");
        }

        log.info("Server: {}", getOpts().getUrl());
        log.info("Username: {}", getOpts().getUsername());
        log.info("File type: {}", fileType);
        if (StringUtils.isNotBlank(getOpts().getProject())) {
            log.info("Project: {}", getOpts().getProject());
        }
        ImmutableList<String> transLang = getOpts().getTransLang();
        if (transLang != null && !transLang.isEmpty()) {
            log.info("Translation language: {}", Joiner.on(",").join(transLang));
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
            }
            throw rpe;
        }

        log.info("Pulling glossary from server");
        Response response;
        try {
            response =
                    client.downloadFile(fileType, transLang, qualifiedName);
        } catch (ResponseProcessingException e) {
            if (e.getResponse().getStatus() == 404) {
                log.info("No glossary file in server");
                return;
            }
            throw e;
        }

        InputStream glossaryFile = response.readEntity(InputStream.class);
        if (glossaryFile == null) {
            log.info("No glossary file in server");
            return;
        }

        String fileName =
                ClientUtil.getFileNameFromHeader(response.getStringHeaders());
        if (fileName == null) {
            log.error("Null filename response from server: " +
                    response.getStatusInfo());
            return;
        }

        File file = new File(fileName);
        PathUtil.makeDirs(file.getParentFile());
        try (OutputStream out = new FileOutputStream(file)) {
            int read;
            byte[] buffer = new byte[1024];
            while ((read = glossaryFile.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } finally {
            glossaryFile.close();
        }
        log.info("Glossary pulled to {}", fileName);
    }
}
