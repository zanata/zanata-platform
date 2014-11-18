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
import org.zanata.rest.client.AsyncProcessClient;
import org.zanata.rest.client.CopyTransClient;
import org.zanata.rest.client.RestClientFactory;

import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Hint;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Warning;
import static org.zanata.client.commands.StringUtil.indent;
import static org.zanata.client.commands.ConsoleInteractorImpl.AnswerValidatorImpl.*;
import static org.zanata.client.commands.Messages._;

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
    // includes and excludes in ConfigurableProjectOptions do not have symmetric
    // setter/getter (set method accepts String but get method returns
    // ImmutableList) therefore we have to keep instance variables here
    private String includes;
    private String excludes;
    private PushCommand pushCommand;
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

        RestClientFactory clientFactory =
                OptionsUtil.createClientFactoryWithoutVersionCheck(pushOptions);
        pushCommand =
                new PushCommand(pushOptions,
                        clientFactory.getCopyTransClient(),
                        clientFactory.getAsyncProcessClient(),
                        clientFactory);
    }

    SourceConfigPrompt promptUser() throws Exception {
        console.printf(Question, _("src.dir.prompt"));
        String localSrcDir = console.expectAnyNotBlankAnswer();
        File srcDir = new File(localSrcDir);
        if (!srcDir.exists()) {
            console.printfln(Warning, _("src.dir.not.exist"),
                    localSrcDir);
            return new SourceConfigPrompt(console, opts).promptUser();
        }
        console.blankLine();
        console.printfln(Hint, _("includes.question"));
        console.printfln(_("includes.usage.1"));
        console.printfln(_("includes.usage.2"));
        console.printfln(_("includes.usage.3"));
        console.printf(Question, _("includes.prompt"));
        includes = console.expectAnyAnswer();
        console.blankLine();
        console.printfln(Hint, _("excludes.question"));
        console.printfln(_("excludes.usage"));
        console.printf(Question, _("excludes.prompt"));
        excludes = console.expectAnyAnswer();

        pushOptions.setSrcDir(srcDir);
        pushOptions.setIncludes(includes);
        pushOptions.setExcludes(excludes);
        console.blankLine();
        docNames = findDocNames();
        if (docNames.isEmpty()) {
            console.printfln(_("no.source.doc.found"));
        } else {
            console.printfln(_("found.source.docs"));
            for (String docName : docNames) {
                console.printfln("%s%s", indent(8), docName);
            }
        }
        console.printf(Question, _("source.doc.confirm.yes.no"));
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

    private Set<String> findDocNames() throws IOException {
        AbstractPushStrategy strategy = pushCommand.getStrategy(pushOptions);
        return strategy.findDocNames(pushOptions.getSrcDir(),
                pushOptions.getIncludes(), pushOptions.getExcludes(),
                pushOptions.getDefaultExcludes(),
                pushOptions.getCaseSensitive(),
                pushOptions.getExcludeLocaleFilenames());
    }

    private void hintAdvancedConfigurations() {
        console.printfln(Hint, _("more.src.options.hint"));
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
        }
        catch (NoSuchMethodException e) {
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
}
