/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.client.commands.init;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.OptionsUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.*;
import static org.zanata.client.commands.ConsoleInteractorImpl.AnswerValidatorImpl.*;
import static org.zanata.client.commands.Messages.get;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class UserConfigHandler {
    private static final Logger log =
            LoggerFactory.getLogger(UserConfigHandler.class);
    private final ConsoleInteractor consoleInteractor;
    private final InitOptions opts;

    public UserConfigHandler(ConsoleInteractor consoleInteractor, InitOptions opts) {
        this.consoleInteractor = consoleInteractor;
        this.opts = opts;
    }

    /**
     * Search for zanata.ini. (If there's none, link them to the help page).
     * Provide a list of servers to choose from.
     * @throws Exception
     */
    @VisibleForTesting
    protected void verifyUserConfig() throws Exception {
        File userConfig = opts.getUserConfig();
        if (!userConfig.exists()) {
            String msg = get("missing.user.config");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
        clearValueSetByConfigurableMojo();
        // read in content and get all available server urls
        HierarchicalINIConfiguration config =
                new HierarchicalINIConfiguration(opts.getUserConfig());
        List<URL> serverUrls = readServerUrlsFromUserConfig(config);

        // apply user config
        if (serverUrls.isEmpty()) {
            String msg = get("missing.server.url");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
        if (serverUrls.size() == 1) {
            opts.setUrl(serverUrls.get(0));
        } else {
            consoleInteractor.printfln(get("found.servers"), opts.getUserConfig().getName());
            List<String> answers = listServerUrlsPrefixedWithNumber(serverUrls);
            consoleInteractor.printf(Question, get("which.server"));
            String chosenNumber =
                    consoleInteractor.expectAnswerWithRetry(expect(answers));
            URL url = serverUrls.get(Integer.parseInt(chosenNumber) - 1);
            consoleInteractor.printfln(Confirmation, get("server.selection"), url);
            opts.setUrl(url);
        }
        OptionsUtil.applyUserConfig(opts, config);
    }

    private void clearValueSetByConfigurableMojo() {
        opts.setUsername(null);
        opts.setUrl(null);
        opts.setKey(null);
    }

    private List<URL> readServerUrlsFromUserConfig(
            HierarchicalINIConfiguration config)
            throws ConfigurationException {
        SubnodeConfiguration servers = config.getSection("servers");
        DataConfiguration dataConfig = new DataConfiguration(servers);
        List<URL> serverUrls = Lists.newArrayList();
        for (Iterator<String> iterator = dataConfig.getKeys(); iterator
                .hasNext();) {
            String key = iterator.next();
            if (key.endsWith(".url")) {
                serverUrls.add(dataConfig.get(URL.class, key));
            }
        }
        return serverUrls;
    }

    private List<String> listServerUrlsPrefixedWithNumber(List<URL> serverUrls) {
        List<String> answers = Lists.newArrayList();
        for (int i = 0; i < serverUrls.size(); i++) {
            URL serverUrl = serverUrls.get(i);
            int number = i + 1;
            consoleInteractor
                    .printf("%d)", number)
                    .printfln(Hint, serverUrl.toString());
            answers.add(number + "");
        }
        return answers;
    }

    public InitOptions getOpts() {
        return opts;
    }
}
