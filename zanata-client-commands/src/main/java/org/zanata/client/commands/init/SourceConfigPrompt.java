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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.BasicOptions;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.push.AbstractPushStrategy;
import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushOptions;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.commands.push.RawPushCommand;
import org.zanata.client.commands.push.RawPushStrategy;
import org.zanata.common.DocumentType;
import org.zanata.rest.client.FileResourceClient;
import org.zanata.rest.client.RestClientFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Hint;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Warning;
import static org.zanata.client.commands.StringUtil.indent;
import static org.zanata.client.commands.ConsoleInteractorImpl.AnswerValidatorImpl.*;
import static org.zanata.common.ProjectType.File;
import static org.zanata.client.commands.Messages.get;

/**
 * Prompt for src dir.
 * Prompt for includes and excludes.
 * Show a sample of the document names.
 * Prompt for acceptance or back to the start.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class SourceConfigPrompt {
    private static final Logger log =
            LoggerFactory.getLogger(SourceConfigPrompt.class);
    private final ConsoleInteractor console;
    private final ConfigurableProjectOptions opts;
    private final PushOptions pushOptions;
    private final SrcDocsFinder
            srcDocsFinder;
    // includes and excludes in ConfigurableProjectOptions do not have symmetric
    // setter/getter (set method accepts String but get method returns
    // ImmutableList) therefore we have to keep instance variables here
    private String includes;
    private String excludes;
    private Set<String> docNames;

    public SourceConfigPrompt(
            ConsoleInteractor console,
            ConfigurableProjectOptions opts)
            throws InvocationTargetException, IllegalAccessException {
        this.console = console;
        this.opts = opts;
        pushOptions = new PushOptionsImpl();
        pushOptions.setProj(opts.getProj());
        pushOptions.setProjectVersion(opts.getProjectVersion());
        pushOptions.setProjectType(opts.getProjectType());
        pushOptions.setUrl(opts.getUrl());
        pushOptions.setUsername(opts.getUsername());
        pushOptions.setKey(opts.getKey());
        pushOptions.setLocaleMapList(opts.getLocaleMapList());

        srcDocsFinder = makeSrcDocFinder();
    }

    SourceConfigPrompt promptUser() throws Exception {
        console.printf(Question, get("src.dir.prompt"));
        String localSrcDir = console.expectAnyNotBlankAnswer();
        File srcDir = new File(localSrcDir);
        if (!srcDir.exists()) {
            console.printfln(Warning, get("src.dir.not.exist"),
                    localSrcDir);
            return new SourceConfigPrompt(console, opts).promptUser();
        }
        console.blankLine();
        console.printfln(Hint, get("includes.question"));
        console.printfln(get("includes.usage.1"));
        console.printfln(get("includes.usage.2"));
        console.printfln(get("includes.usage.3"));
        console.printf(Question, get("includes.prompt"));
        includes = console.expectAnyAnswer();
        console.blankLine();
        console.printfln(Hint, get("excludes.question"));
        console.printfln(get("excludes.usage"));
        console.printf(Question, get("excludes.prompt"));
        excludes = console.expectAnyAnswer();

        pushOptions.setSrcDir(srcDir);
        pushOptions.setIncludes(includes);
        pushOptions.setExcludes(excludes);


        // if project type is file, we need to ask file type in order to find source documents
        if (File.name().equalsIgnoreCase(pushOptions.getProjectType())) {

            console.blankLine();
            console.printfln(Question, get("project.file.type.question"));

            // this answer is not persisted in zanata.xml so user will still need to type it when they do the actual push
            console.printfln(Hint, PushOptionsImpl.fileTypeHelp);
            console.printf(Question, get("file.type.prompt"));
            String answer = console.expectAnyNotBlankAnswer();
            ((PushOptionsImpl) pushOptions).setFileTypes(answer);
        }

        console.blankLine();
        docNames = findDocNames();
        if (docNames.isEmpty()) {
            console.printfln(get("no.source.doc.found"));
        } else {
            console.printfln(get("found.source.docs"));
            for (String docName : docNames) {
                console.printfln("%s%s", indent(8), docName);
            }
        }
        console.printf(Question, get("source.doc.confirm.yes.no"));
        String answer = console.expectAnswerWithRetry(YES_NO);
        if (answer.toLowerCase().startsWith("n")) {
            hintAdvancedConfigurations();
            return new SourceConfigPrompt(console, opts).promptUser();
        }
        opts.setSrcDir(srcDir);
        opts.setIncludes(includes);
        opts.setExcludes(excludes);
        console.blankLine();
        return this;
    }

    @VisibleForTesting
    protected RestClientFactory getClientFactory(PushOptions pushOptions) {
        return OptionsUtil
                .createClientFactoryWithoutVersionCheck(pushOptions);
    }

    private Set<String> findDocNames() throws IOException {
        return srcDocsFinder.findSrcDocNames();
    }

    private void hintAdvancedConfigurations() {
        console.printfln(Hint, get("more.src.options.hint"));
        console.printfln(Hint,
                " - %s",
                getUsageFromOptionAnnotation(PushOptionsImpl.class,
                        "setDefaultExcludes", boolean.class));
        console.printfln(Hint,
                " - %s",
                getUsageFromOptionAnnotation(PushOptionsImpl.class,
                        "setCaseSensitive", boolean.class));
        console.printfln(Hint,
                " - %s",
                getUsageFromOptionAnnotation(PushOptionsImpl.class,
                        "setExcludeLocaleFilenames", boolean.class));
    }

    private static <T> String getUsageFromOptionAnnotation(
            Class<? extends BasicOptions> optionsClass, String methodName,
            Class<T> argType) {
        try {
            return optionsClass
                    .getMethod(methodName, argType).getAnnotation(
                            Option.class).usage()
                    // the usage text is not very well formatted (contains new line)
                    .replaceAll(System.getProperty("line.separator"), " ");
        } catch (NoSuchMethodException e) {
            log.error("can not find method: {} on class {}", methodName, optionsClass);
            return methodName;
        }
    }

    public String getIncludes() {
        return includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public Set<String> getDocNames() {
        return docNames;
    }

    private SrcDocsFinder makeSrcDocFinder() {
        String projectType = pushOptions.getProjectType();
        if (projectType.equalsIgnoreCase(File.name())) {
            return new RawSrcDocsFinder(pushOptions);
        } else {
            return new OtherSrcDocsFinder(pushOptions);
        }
    }

    interface SrcDocsFinder {
        Set<String> findSrcDocNames();
    }

    class RawSrcDocsFinder implements SrcDocsFinder {

        private final RawPushStrategy strategy;

        RawSrcDocsFinder(PushOptions pushOptions) {
            strategy = new RawPushStrategy();
            strategy.setPushOptions(pushOptions);
        }

        @Override
        public Set<String> findSrcDocNames() {
            PushOptions opts = strategy.getOpts();
            ImmutableList<String> extensions = filteredFileExtensions(opts);
            return ImmutableSet.copyOf(strategy.getSrcFiles(
                    opts.getSrcDir(), opts.getIncludes(),
                    opts.getExcludes(), extensions,
                    opts.getDefaultExcludes(),
                    opts.getCaseSensitive()));
        }

        private ImmutableList<String> filteredFileExtensions(PushOptions opts) {
            // mostly duplicated in RawPushCommand
            RestClientFactory clientFactory = getClientFactory(opts);
            RawPushCommand rawPushCommand =
                    new RawPushCommand(opts, clientFactory, console);
            FileResourceClient client =
                    clientFactory.getFileResourceClient();
            List<DocumentType> rawDocumentTypes = client.acceptedFileTypes();
            Map<DocumentType, Set<String>> filteredDocTypes =
                    rawPushCommand.validateFileTypes(rawDocumentTypes,
                            opts.getFileTypes());

            if (filteredDocTypes.isEmpty()) {
                log.info("no valid types specified; nothing to do");
                return ImmutableList.of();
            }

            ImmutableList.Builder<String> sourceFileExtensionsBuilder =
                    ImmutableList.builder();
            for (Set<String> filteredSourceExtensions : filteredDocTypes
                    .values()) {
                sourceFileExtensionsBuilder.addAll(filteredSourceExtensions);
            }
            return sourceFileExtensionsBuilder.build();
        }
    }

    class OtherSrcDocsFinder implements SrcDocsFinder {
        private final AbstractPushStrategy strategy;
        private PushOptions opts;

        OtherSrcDocsFinder(PushOptions pushOptions) {
            opts = pushOptions;
            RestClientFactory clientFactory = getClientFactory(pushOptions);
            PushCommand pushCommand =
                    new PushCommand(pushOptions,
                            clientFactory.getCopyTransClient(),
                            clientFactory.getAsyncProcessClient(),
                            clientFactory);
            strategy = pushCommand.getStrategy(pushOptions);
        }

        @Override
        public Set<String> findSrcDocNames() {
            try {
                return strategy
                        .findDocNames(opts.getSrcDir(), opts.getIncludes(),
                                opts.getExcludes(),
                                opts.getDefaultExcludes(),
                                opts.getCaseSensitive(),
                                opts.getExcludeLocaleFilenames());
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
    }
}

