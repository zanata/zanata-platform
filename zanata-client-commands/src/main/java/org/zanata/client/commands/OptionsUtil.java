package org.zanata.client.commands;

import java.io.File;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.config.ConfigUtil;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.ZanataConfig;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.util.VersionUtility;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class OptionsUtil {
    private static final Logger log = LoggerFactory
            .getLogger(OptionsUtil.class);

    /**
     * Loads the config files (controlled by the property userConfig) to supply
     * any values which haven't already been set.
     *
     * @throws Exception
     */
    public static void applyConfigFiles(ConfigurableOptions opts)
            throws ConfigurationException, JAXBException {
        if (opts instanceof ConfigurableProjectOptions) {
            ConfigurableProjectOptions projOpts =
                    (ConfigurableProjectOptions) opts;
            if (projOpts.getProjectConfig() != null) {
                JAXBContext jc = JAXBContext.newInstance(ZanataConfig.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                File projectConfigFile = projOpts.getProjectConfig();
                if (projectConfigFile.exists()) {
                    log.info("Loading project config from {}",
                            projectConfigFile);
                    ZanataConfig projectConfig =
                            (ZanataConfig) unmarshaller
                                    .unmarshal(projectConfigFile);
                    // local project config is supposed to override user's
                    // zanata.ini,
                    // so we apply it first
                    applyProjectConfig(projOpts, projectConfig);
                } else {
                    log.warn("Project config file '{}' not found; ignoring.",
                            projectConfigFile);
                }
            }
        }
        if (opts.getUserConfig() != null) {
            if (opts.getUserConfig().exists()) {
                log.info("Loading user config from {}", opts.getUserConfig());
                HierarchicalINIConfiguration dataConfig =
                        new HierarchicalINIConfiguration(opts.getUserConfig());
                applyUserConfig(opts, dataConfig);
            } else {
                System.err.printf(
                        "User config file '%s' not found; ignoring.%n",
                        opts.getUserConfig());
            }
        }
    }

    /**
     * Applies values from the project configuration unless they have been set
     * directly via parameters.
     *
     * @param config
     */
    private static void applyProjectConfig(ConfigurableProjectOptions opts,
            ZanataConfig config) {
        if (opts.getProj() == null) {
            opts.setProj(config.getProject());
        }
        if (opts.getUrl() == null) {
            opts.setUrl(config.getUrl());
        }
        if (opts.getProjectVersion() == null) {
            opts.setProjectVersion(config.getProjectVersion());
        }
        if (opts.getProjectType() == null) {
            opts.setProjectType(config.getProjectType());
        }
        applySrcDirAndTransDirFromProjectConfig(opts, config);
        applyIncludesAndExcludesFromProjectConfig(opts, config);
        LocaleList locales = config.getLocales();
        opts.setLocaleMapList(locales);

        if (opts.getCommandHooks().isEmpty() && config.getHooks() != null) {
            opts.setCommandHooks(config.getHooks());
        }
    }


    /**
     * Note: command line options take precedence over pom.xml which
     * takes precedence over zanata.xml.
     *
     * @see org.zanata.client.commands.OptionMismatchChecker
     * @param opts
     *            options
     * @param config
     *            config from project configuration file i.e. zanata.xml
     */
    @VisibleForTesting
    protected static void applySrcDirAndTransDirFromProjectConfig(
            ConfigurableProjectOptions opts, ZanataConfig config) {
        // apply srcDir configuration
        OptionMismatchChecker<File> srcDirChecker =
                OptionMismatchChecker.from(opts.getSrcDir(),
                        config.getSrcDirAsFile(),
                        "Source directory");

        if (srcDirChecker.hasValueInConfigOnly()) {
            opts.setSrcDir(config.getSrcDirAsFile());
        }
        srcDirChecker.logHintIfNotDefinedInConfig(
                String.format("<src-dir>%s</src-dir>", opts.getSrcDir()));
        srcDirChecker.logWarningIfValuesMismatch();

        // apply transDir configuration
        OptionMismatchChecker<File> transDirChecker = OptionMismatchChecker
                .from(opts.getTransDir(), config.getTransDirAsFile(),
                        "Translation directory");
        if (transDirChecker.hasValueInConfigOnly()) {
            opts.setTransDir(config.getTransDirAsFile());
        }
        transDirChecker.logHintIfNotDefinedInConfig(String.format(
                "<trans-dir>%s</trans-dir>", opts.getTransDir()));
        transDirChecker.logWarningIfValuesMismatch();
    }

    /**
     * Note: command line options take precedence over pom.xml which
     * takes precedence over zanata.xml.
     *
     * @see org.zanata.client.commands.OptionMismatchChecker
     * @param opts
     *            options
     * @param config
     *            config from project configuration file i.e. zanata.xml
     */
    protected static void applyIncludesAndExcludesFromProjectConfig(
            ConfigurableProjectOptions opts, ZanataConfig config) {
        OptionMismatchChecker<ImmutableList<String>> includesChecker =
                OptionMismatchChecker
                        .from(opts.getIncludes(), config.getIncludesAsList(),
                                "Includes");
        if (includesChecker.hasValueInConfigOnly()) {
            opts.setIncludes(config.getIncludes());
        }
        Joiner commaJoiner = Joiner.on(",");
        includesChecker.logHintIfNotDefinedInConfig(String.format(
                "<includes>%s</includes>",
                commaJoiner.join(opts.getIncludes())));
        includesChecker.logWarningIfValuesMismatch();

        OptionMismatchChecker<ImmutableList<String>> excludesChecker =
                OptionMismatchChecker
                        .from(opts.getExcludes(), config.getExcludesAsList(),
                                "Excludes");
        if (excludesChecker.hasValueInConfigOnly()) {
            opts.setExcludes(config.getExcludes());
        }
        excludesChecker
                .logHintIfNotDefinedInConfig(
                        String.format("<excludes>%s</excludes>",
                                commaJoiner.join(opts.getExcludes())));
        excludesChecker.logWarningIfValuesMismatch();
    }

    /**
     * Applies values from the user's personal configuration unless they have
     * been set directly (by parameters or by project configuration).
     *
     * @param config
     */
    public static void applyUserConfig(ConfigurableOptions opts,
            HierarchicalINIConfiguration config) {
        if (!opts.isDebugSet()) {
            Boolean debug = config.getBoolean("defaults.debug", null);
            if (debug != null)
                opts.setDebug(debug);
        }

        if (!opts.isErrorsSet()) {
            Boolean errors = config.getBoolean("defaults.errors", null);
            if (errors != null)
                opts.setErrors(errors);
        }

        if (!opts.isQuietSet()) {
            Boolean quiet = config.getBoolean("defaults.quiet", null);
            if (quiet != null)
                opts.setQuiet(quiet);
        }
        if ((opts.getUsername() == null || opts.getKey() == null)
                && opts.getUrl() != null) {
            SubnodeConfiguration servers = config.getSection("servers");
            String prefix = ConfigUtil.findPrefix(servers, opts.getUrl());
            if (prefix != null) {
                if (opts.getUsername() == null) {
                    opts.setUsername(servers.getString(prefix + ".username",
                            null));
                }
                if (opts.getKey() == null) {
                    opts.setKey(servers.getString(prefix + ".key", null));
                }
            }
        }
    }

    /**
     * Creates proxy factory that will perform an eager REST version check.
     */
    public static ZanataProxyFactory createRequestFactory(
            ConfigurableOptions opts) {
        try {
            checkMandatoryOptsForRequestFactory(opts);
            return new ZanataProxyFactory(opts.getUrl().toURI(),
                    opts.getUsername(), opts.getKey(),
                    VersionUtility.getAPIVersionInfo(), opts.getLogHttp(),
                    opts.isDisableSSLCert());
        } catch (URISyntaxException e) {
            throw new ConfigException(e);
        }
    }

    private static void checkMandatoryOptsForRequestFactory(
            ConfigurableOptions opts) {
        if (opts.getUrl() == null) {
            throw new ConfigException("Server URL must be specified");
        }
        if (opts.getUsername() == null) {
            throw new ConfigException("Username must be specified");
        }
        if (opts.getKey() == null) {
            throw new ConfigException("API key must be specified");
        }
        if (opts.isDisableSSLCert()) {
            log.warn("SSL certificate verification will be disabled. You should consider adding the certificate instead of disabling it.");
        }
    }

    /**
     * Creates proxy factory that will NOT perform an eager REST version check.
     * You can call
     * org.zanata.rest.client.ZanataProxyFactory#performVersionCheck()
     * afterwards.
     */
    public static ZanataProxyFactory createRequestFactoryWithoutVersionCheck(
            ConfigurableProjectOptions opts) {
        try {
            checkMandatoryOptsForRequestFactory(opts);
            return new ZanataProxyFactory(opts.getUrl().toURI(),
                    opts.getUsername(), opts.getKey(),
                    VersionUtility.getAPIVersionInfo(), opts.getLogHttp(),
                    opts.isDisableSSLCert(), false);
        } catch (URISyntaxException e) {
            throw new ConfigException(e);
        }
    }

}
