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

import static org.apache.commons.io.Charsets.UTF_8;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Confirmation;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Hint;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Warning;
import static org.zanata.client.commands.Messages._;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.ConsoleInteractorImpl;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.config.ZanataConfig;
import org.zanata.client.util.VersionComparator;
import org.zanata.rest.client.ZanataProxyFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class InitCommand extends ConfigurableCommand<InitOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(InitCommand.class);
    private static final ZanataProxyFactory mockFactory =
            MockZanataProxyFactory.mockFactory;
    private static final String ITERATION_URL = "%siteration/view/%s/%s";
    private ConsoleInteractor console;
    private ZanataProxyFactory requestFactory;
    private ProjectConfigHandler projectConfigHandler;
    private UserConfigHandler userConfigHandler;

    public InitCommand(InitOptions opts) {
        // we don't have all mandatory information yet. Can't create a real
        // proxy factory.
        super(opts, mockFactory);
        console = new ConsoleInteractorImpl();
        projectConfigHandler =
                new ProjectConfigHandler(console, getOpts());
        userConfigHandler = new UserConfigHandler(console, getOpts());
    }

    @VisibleForTesting
    protected InitCommand(InitOptions opts,
            ConsoleInteractor console,
            ZanataProxyFactory requestFactory) {
        super(opts, mockFactory);
        this.console = console;
        this.requestFactory = requestFactory;
        projectConfigHandler =
                new ProjectConfigHandler(console, getOpts());
        userConfigHandler = new UserConfigHandler(console, getOpts());
    }

    @Override
    protected void run() throws Exception {
        // Search for zanata.ini
        userConfigHandler.verifyUserConfig();

        ensureServerVersion();

        // If there's a zanata.xml, ask the user
        projectConfigHandler.handleExistingProjectConfig();

        // Select or create a project and version
        new ProjectPrompt(console, getOpts(),
                getRequestFactory(),
                new ProjectIterationPrompt(console, getOpts(),
                        getRequestFactory()))
                .selectOrCreateNewProjectAndVersion();

        advancedSettingsReminder();

        downloadZanataXml(getOpts().getProj(), getOpts().getProjectVersion(),
                new File("zanata.xml"));

        applyConfigFileSilently();

        // Prompt for src dir.
        SourceConfigPrompt sourceConfigPrompt =
                new SourceConfigPrompt(console, getOpts()).promptUser();

        //Prompt for trans dir, .
        new TransConfigPrompt(console, getOpts(),
                        sourceConfigPrompt.getDocNames()).promptUser();

        writeToConfig(getOpts().getSrcDir(),
                sourceConfigPrompt.getIncludes(), sourceConfigPrompt
                        .getExcludes(), getOpts().getTransDir(),
                getOpts().getProjectConfig());

        // Offer a few useful commands plus urls (what next)
        displayAdviceAboutWhatIsNext(projectConfigHandler.hasOldConfig());
    }

    @VisibleForTesting
    protected void ensureServerVersion() {
        String serverVersion =
                getRequestFactory().getServerVersionInfo().getVersionNo();

        if (new VersionComparator().compare(serverVersion, "3.4.0") < 0) {
            console.printfln(Warning, _("server.incompatible"));
            console.printfln(Hint, _("server.incompatible.hint"));
            throw new RuntimeException(_("server.incompatible"));
        }
    }

    private void displayAdviceAboutWhatIsNext(boolean hasOldConfig) {
        console.printfln(_("what.next"));
        if (hasOldConfig) {
            console.printfln(_("compare.project.config"),
                    projectConfigHandler.getBackup());
        }
        console.printfln(_("view.project"),
                getProjectIterationUrl(getOpts().getUrl(), getOpts().getProj(),
                        getOpts().getProjectVersion()));
        if (isInvokedByMaven()) {
            console.printfln(_("mvn.push.source"));
            console.printfln(_("mvn.push.both"));
            console.printfln(_("mvn.push.trans"));
            console.printfln(_("mvn.help"));
        } else {
            console.printfln(_("cli.push.source"));
            console.printfln(_("cli.push.both"));
            console.printfln(_("cli.push.trans"));
            console.printfln(_("cli.help"));
        }
        console.printfln(_("browse.online.help"));
    }

    private void advancedSettingsReminder() {
        ConfigurableProjectOptions opts = getOpts();
        console.printfln(Warning, _("customize.languages.warning"));
        console.printfln(Hint, _("view.project"),
                getProjectIterationUrl(opts.getUrl(), opts.getProj(),
                        opts.getProjectVersion()));
        console.printf(Question, _("continue.yes.no"));
        console.expectYes();
    }

    private void applyConfigFileSilently()
            throws ConfigurationException, JAXBException {
        ConfigurableProjectOptions opts = getOpts();
        org.apache.log4j.Logger logger =
                LogManager.getLogger(OptionsUtil.class);
        Level preLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
        OptionsUtil.applyConfigFiles(opts);
        logger.setLevel(preLevel);
        console.printfln(
                Confirmation, _("project.version.type.confirmation"),
                opts.getProjectType(), opts.getProj(), opts
                        .getProjectVersion());
    }

    private boolean isInvokedByMaven() {
        return getOpts().getClass().getPackage().getName().contains("maven");
    }

    private static String getProjectIterationUrl(URL server, String projectSlug,
            String iterationSlug) {
        return String.format(ITERATION_URL, server,
                projectSlug, iterationSlug);
    }


    public static void offerRetryOnServerError(ClientResponse response,
            ConsoleInteractor consoleInteractor) {
        consoleInteractor.printfln(Warning, _("server.error"),
                response.getEntity(String.class));
        consoleInteractor.printf(Question, _("server.error.try.again"));
        consoleInteractor.expectYes();
    }

    public ZanataProxyFactory getRequestFactory() {
        if (requestFactory != null) {
            return requestFactory;
        } else {
            requestFactory = OptionsUtil.createRequestFactory(getOpts());
            console.blankLine();
            return requestFactory;
        }
    }

    /**
     * Downloads the zanata.xml config file using REST api.
     *
     * @param projectId
     *            project slug
     * @param iterationId
     *            iteration slug
     * @param configFileDest
     *            project config destination
     */
    @VisibleForTesting
    protected void downloadZanataXml(String projectId, String iterationId,
            File configFileDest) throws IOException {
        ClientResponse response =
                getRequestFactory().getProjectIteration(projectId, iterationId)
                        .sampleConfiguration();
        if (response.getStatus() >= 399) {
            offerRetryOnServerError(response, console);
            downloadZanataXml(projectId, iterationId, configFileDest);
            return;
        }
        boolean created = configFileDest.createNewFile();
        Preconditions.checkState(created,
                "Can not create %s. Make sure permission is writable.",
                configFileDest);

        String content = (String) response.getEntity(String.class);
        log.debug("project config from the server:\n{}", content);
        FileUtils.write(configFileDest, content, UTF_8);
        getOpts().setProjectConfig(configFileDest);

    }

    @VisibleForTesting
    protected void writeToConfig(File srcDir, String includes, String excludes,
            File transDir, File configFile)
            throws Exception {
        JAXBContext jc = JAXBContext.newInstance(ZanataConfig.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ZanataConfig currentConfig =
                (ZanataConfig) unmarshaller.unmarshal(configFile);
        currentConfig.setSrcDir(srcDir.getPath());
        // try to avoid empty tags
        currentConfig.setIncludes(Strings.emptyToNull(includes));
        currentConfig.setExcludes(Strings.emptyToNull(excludes));
        currentConfig.setHooks(null);
        if (currentConfig.getLocales().isEmpty()) {
            currentConfig.setLocales(null);
        }
        currentConfig.setTransDir(transDir.getPath());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(currentConfig, configFile);

        console.printfln(Confirmation, "Project config created at:%s",
                getOpts().getProjectConfig());
    }

    // we don't have all mandatory information yet
    private static class MockZanataProxyFactory extends ZanataProxyFactory {
        private static final MockZanataProxyFactory mockFactory =
                new MockZanataProxyFactory();

        private MockZanataProxyFactory() {
            super();
        }
    }
}
