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
package org.zanata.security;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import javax.annotation.Nullable;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.web.config.AbstractSAMLConfigurationProvider;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This file is responsible to load the picketlink configuration file (path
 * given by system property).
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ExternalSAMLConfigurationProvider
        extends AbstractSAMLConfigurationProvider {
    private static final Logger log =
            LoggerFactory.getLogger(ExternalSAMLConfigurationProvider.class);

    private static final String DEFAULT_CONFIG_PROTOCOL = "file://";
    private static @Nullable String configFile = System.getProperty("picketlink.file");

    @Override
    public IDPType getIDPConfiguration() throws ProcessingException {
        throw new RuntimeException(ErrorCodes.ILLEGAL_METHOD_CALLED);
    }

    @Override
    public SPType getSPConfiguration() throws ProcessingException {

        try {
            Optional<InputStream> inputStream = readConfigurationFile();
            if (inputStream.isPresent()) {
                return ConfigurationUtil
                        .getSPConfiguration(inputStream.get());
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load SP configuration: "
                    + getConfigurationFilePath(), e);
        }
        return null;
    }

    @Override
    public PicketLinkType getPicketLinkConfiguration()
            throws ProcessingException {
        try {
            Optional<InputStream> inputStream = readConfigurationFile();
            if (inputStream.isPresent()) {
                return ConfigurationUtil.getConfiguration(inputStream.get());
            }
        } catch (ParsingException | ConfigurationException e) {
            throw new RuntimeException(
                    "Could not load PicketLink configuration: "
                            + getConfigurationFilePath(),
                    e);
        }
        return null;
    }

    /**
     * Returns the picketlink configuration file path including protocol.
     */
    private static String getConfigurationFilePath() {
        return DEFAULT_CONFIG_PROTOCOL + configFile;
    }

    private static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.canRead();
    }

    /**
     * Loads the configuration file
     */
    private static Optional<InputStream> readConfigurationFile()
            throws ConfigurationException {
        if (configFile == null || !isFileExists(configFile)) {
            log.info("picketlink.xml can not be found: {}", configFile);
            return Optional.empty();
        }
        URL configurationFileURL;

        try {
            configurationFileURL = Thread.currentThread()
                    .getContextClassLoader().getResource(configFile);

            if (configurationFileURL == null) {
                configurationFileURL = new URL(getConfigurationFilePath());
            }

            return Optional.of(configurationFileURL.openStream());
        } catch (Exception e) {
            throw new RuntimeException(
                    "The file could not be loaded: " + configFile, e);
        }
    }
}
