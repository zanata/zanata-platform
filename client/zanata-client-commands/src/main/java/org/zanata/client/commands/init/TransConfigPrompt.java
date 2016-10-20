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
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.DocNameWithExt;
import org.zanata.client.commands.TransFileResolver;
import org.zanata.client.commands.DocNameWithoutExt;
import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.ProjectType;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Hint;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;
import static org.zanata.client.commands.StringUtil.indent;
import static org.zanata.client.commands.ConsoleInteractorImpl.AnswerValidatorImpl.*;
import static org.zanata.client.commands.Messages.get;

/**
 * Prompt for trans dir,
 * Show preview of translated files (e.g. if pull where the file will be).
 * Prompt for acceptance or back to the start.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class TransConfigPrompt {
    private final ConsoleInteractor console;
    private final ConfigurableProjectOptions opts;
    private final Set<String> srcFilesSample;
    private final PullOptionsImpl pullOptions;
    private final TransFilePathFinder
            transFilePathFinder;
    private int remainingFileNumber;

    public TransConfigPrompt(ConsoleInteractor console,
            ConfigurableProjectOptions opts, Set<String> srcFiles) {
        this.console = console;
        this.opts = opts;
        this.srcFilesSample = ImmutableSet.copyOf(Iterables.limit(srcFiles, 5));
        remainingFileNumber = srcFiles.size() - srcFilesSample.size();
        pullOptions = new PullOptionsImpl();
        // copy over options
        pullOptions.setUrl(opts.getUrl());
        pullOptions.setUsername(opts.getUsername());
        pullOptions.setKey(opts.getKey());
        pullOptions.setProj(opts.getProj());
        pullOptions.setProjectVersion(opts.getProjectVersion());
        pullOptions.setProjectType(opts.getProjectType());
        pullOptions.setLocaleMapList(opts.getLocaleMapList());

        transFilePathFinder = makeTransFilePathFinder(pullOptions);
    }

    TransConfigPrompt promptUser() throws Exception {
        console.printf(Question, get("trans.dir.prompt"));
        String localTransDir = console.expectAnyNotBlankAnswer();
        File transDir = new File(localTransDir);
        pullOptions.setTransDir(transDir);

        LocaleList localeMapList = pullOptions.getLocaleMapList();
        LocaleMapping localeMapping = getSampleLocaleMapping(localeMapList);

        Iterable<String> transFiles = Iterables.transform(srcFilesSample,
                new ToTransFileNameFunction(transFilePathFinder, localeMapping));
        console.printfln(Hint, get("trans.doc.preview"), localeMapping.getLocale());
        for (String transFile : transFiles) {
            console.printfln("%s%s", indent(8), transFile);
        }
        if (remainingFileNumber > 0) {
            console.printfln(get("remaining.files"), remainingFileNumber);
        }
        console.printf(Question, get("trans.dir.confirm.yes.no"));
        String answer = console.expectAnswerWithRetry(YES_NO);
        if (answer.toLowerCase().startsWith("n")) {
            return new TransConfigPrompt(console, opts, srcFilesSample)
                    .promptUser();
        }
        opts.setTransDir(transDir);
        console.blankLine();
        return this;
    }

    private TransFilePathFinder makeTransFilePathFinder(PullOptions opts) {
        if (ProjectType.File.name().equalsIgnoreCase(opts.getProjectType())) {
            return new RawTransFilePathFinder(opts);
        } else {
            return new OtherTransFilePathFinder(opts);
        }
    }

    private static LocaleMapping
            getSampleLocaleMapping(LocaleList localeMapList) {
        LocaleMapping localeMapping;
        if (localeMapList == null || localeMapList.isEmpty()) {
            localeMapping = new LocaleMapping("zh");
        } else {
            localeMapping = localeMapList.get(0);
        }
        return localeMapping;
    }

    private static class ToTransFileNameFunction
            implements Function<String, String> {
        private final TransFilePathFinder transFilePathFinder;
        private final LocaleMapping localeMapping;

        public ToTransFileNameFunction(TransFilePathFinder transFilePathFinder,
                LocaleMapping localeMapping) {
            this.transFilePathFinder = transFilePathFinder;
            this.localeMapping = localeMapping;
        }

        @Override
        public String apply(String input) {
            return transFilePathFinder.getTransFileToWrite(input, localeMapping);

        }
    }

    interface TransFilePathFinder {
        String getTransFileToWrite(String srcDoc, LocaleMapping localeMapping);
    }

    static class RawTransFilePathFinder implements TransFilePathFinder {
        private final TransFileResolver transFileResolver;

        RawTransFilePathFinder(PullOptions opts) {
            transFileResolver = new TransFileResolver(opts);
        }

        @Override
        public String getTransFileToWrite(String srcDoc,
                LocaleMapping localeMapping) {
            String targetFileExt = FilenameUtils
                    .getExtension(srcDoc);

            Optional<String> translationFileExtension =
                    Optional.fromNullable(targetFileExt);
            File file = transFileResolver.resolveTransFile(
                    DocNameWithExt.from(srcDoc),
                    localeMapping, translationFileExtension);
            return file.getPath();
        }
    }

    static class OtherTransFilePathFinder implements TransFilePathFinder {
        private final TransFileResolver transFileResolver;

        OtherTransFilePathFinder(PullOptions opts) {
            this.transFileResolver = new TransFileResolver(opts);
        }

        @Override
        public String getTransFileToWrite(String srcDoc,
                LocaleMapping localeMapping) {
            File transFile = transFileResolver.getTransFile(
                    DocNameWithoutExt.from(srcDoc), localeMapping);
            return transFile.getPath();
        }
    }
}
