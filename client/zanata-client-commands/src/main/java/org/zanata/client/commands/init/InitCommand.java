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
import static org.zanata.client.commands.Messages.get;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.ConsoleInteractorImpl;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.config.ZanataConfig;
import org.zanata.client.util.VersionComparator;
import org.zanata.rest.client.ProjectIterationClient;
import org.zanata.rest.client.RestClientFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class InitCommand extends ConfigurableCommand<InitOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(InitCommand.class);
    private static final String ITERATION_URL = "%siteration/view/%s/%s";
    private ConsoleInteractor console;
    private ProjectConfigHandler projectConfigHandler;
    private UserConfigHandler userConfigHandler;

    public InitCommand(InitOptions opts) {
        // we don't have all mandatory information yet (server URL etc)
        super(opts, new RestClientFactory() {});
        console = new ConsoleInteractorImpl(opts);
        projectConfigHandler =
                new ProjectConfigHandler(console, getOpts());
        userConfigHandler = new UserConfigHandler(console, getOpts());
    }

    @VisibleForTesting
    protected InitCommand(InitOptions opts, ConsoleInteractor console) {
        this(opts, console, new RestClientFactory() {});
    }

    @VisibleForTesting
    protected InitCommand(InitOptions opts,
            ConsoleInteractor console,
            RestClientFactory restClientFactory) {
        super(opts, restClientFactory);
        this.console = console;
        projectConfigHandler =
                new ProjectConfigHandler(console, getOpts());
        userConfigHandler = new UserConfigHandler(console, getOpts());
    }

    @Override
    protected void run() throws Exception {
        // Search for zanata.ini
        userConfigHandler.verifyUserConfig();

        setClientFactory(OptionsUtil.createClientFactory(getOpts()));

        ensureServerVersion();

        // If there's a zanata.xml, ask the user
        projectConfigHandler.handleExistingProjectConfig();

        // Select or create a project and version
        new ProjectPrompt(console, getOpts(),
                new ProjectIterationPrompt(console, getOpts(),
                        getClientFactory()), getClientFactory())
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
                getClientFactory().getServerVersionInfo().getVersionNo();

        if (new VersionComparator().compare(serverVersion, "3.4.0") < 0) {
            console.printfln(Warning, get("server.incompatible"));
            console.printfln(Hint, get("server.incompatible.hint"));
            throw new RuntimeException(get("server.incompatible"));
        }
    }

    private void displayAdviceAboutWhatIsNext(boolean hasOldConfig) {
        console.printfln(get("what.next"));
        if (hasOldConfig) {
            console.printfln(get("compare.project.config"),
                    projectConfigHandler.getBackup());
        }
        console.printfln(get("view.project"),
                getProjectIterationUrl(getOpts().getUrl(), getOpts().getProj(),
                        getOpts().getProjectVersion()));
        if (isInvokedByMaven()) {
            console.printfln(get("mvn.push.source"));
            console.printfln(get("mvn.push.both"));
            console.printfln(get("mvn.push.trans"));
            console.printfln(get("mvn.help"));
        } else {
            console.printfln(get("cli.push.source"));
            console.printfln(get("cli.push.both"));
            console.printfln(get("cli.push.trans"));
            console.printfln(get("cli.help"));
        }
        console.printfln(get("browse.online.help"));
    }

    private void advancedSettingsReminder() {
        ConfigurableProjectOptions opts = getOpts();
        console.printfln(Warning, get("customize.languages.warning"));
        console.printfln(Hint, get("view.project"),
                getProjectIterationUrl(opts.getUrl(), opts.getProj(),
                        opts.getProjectVersion()));
        console.printf(Question, get("continue.yes.no"));
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
                Confirmation, get("project.version.type.confirmation"),
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


    public static void offerRetryOnServerError(Exception e,
            ConsoleInteractor consoleInteractor) {
        consoleInteractor.printfln(Warning, get("server.error"),
                Throwables.getRootCause(e).getMessage());
        consoleInteractor.printf(Question, get("server.error.try.again"));
        consoleInteractor.expectYes();
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
        ProjectIterationClient projectIterationClient = getClientFactory()
                .getProjectIterationClient(projectId, iterationId);

        String content;
        try {
            content = projectIterationClient.sampleConfiguration();
        } catch (Exception e) {
            offerRetryOnServerError(e, console);
            downloadZanataXml(projectId, iterationId, configFileDest);
            return;
        }

        boolean created = configFileDest.createNewFile();
        Preconditions.checkState(created,
                "Can not create %s. Make sure permission is writable.",
                configFileDest);

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

}
