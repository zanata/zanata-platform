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

package org.zanata.client.commands.push;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.zanata.client.TestUtils.createAndAddLocaleMapping;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.TempTransFileRule;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class RawPushStrategyTest {
    @Rule
    public TempTransFileRule tempFileRule = new TempTransFileRule();
    private RawPushStrategy strategy;
    private PushOptionsImpl opts;
    @Captor
    private ArgumentCaptor<File> fileCaptor;
    @Mock
    private RawPushStrategy.TranslationFilesVisitor visitor;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        strategy = new RawPushStrategy();
        opts = new PushOptionsImpl();
        opts.setLocaleMapList(new LocaleList());
        strategy.setPushOptions(opts);
        opts.setTransDir(tempFileRule.getTransDir());
        opts.setProjectType("file");
    }

    @Test
    public void canVisitTranslationFileWithoutFileMapping() throws IOException {
        // with translation
        LocaleMapping deMapping = createAndAddLocaleMapping("de",
                Optional.<String>absent(), opts);
        // with translation and has map-from
        LocaleMapping zhMapping =
                createAndAddLocaleMapping("zh-CN",
                        Optional.of("zh-Hans"),
                        opts);
        // no translation
        opts.getLocaleMapList().add(new LocaleMapping("ja"));

        File deTransFile =
                tempFileRule.createTransFileRelativeToTransDir(
                        "de/src/test.odt");
        File zhTransFile =
                tempFileRule.createTransFileRelativeToTransDir(
                        "zh-Hans/src/test.odt");

        strategy.visitTranslationFiles("src/test.odt", visitor,
            Optional.<String>absent());

        verify(visitor).visit(eq(deMapping), fileCaptor.capture());
        assertThat(fileCaptor.getValue(), equalTo(deTransFile));

        verify(visitor).visit(eq(zhMapping), fileCaptor.capture());
        assertThat(fileCaptor.getValue(), equalTo(zhTransFile));

        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void canVisitTranslationFileUsingFileMapping() throws IOException {
        // with translation
        LocaleMapping deMapping = createAndAddLocaleMapping("de",
                Optional.<String>absent(), opts);
        // with translation and has map-from
        LocaleMapping zhMapping =
                createAndAddLocaleMapping("zh-CN",
                        Optional.of("zh-Hans"),
                        opts);
        // no translation
        opts.getLocaleMapList().add(new LocaleMapping("ja"));

        File deTransFile =
                tempFileRule.createTransFileRelativeToTransDir("de/test.odt");
        File zhTransFile =
                tempFileRule.createTransFileRelativeToTransDir(
                        "zh-Hans/test.odt");

        opts.setFileMappingRules(Lists.newArrayList(
                new FileMappingRule("**/*.odt",
                        "{locale}/{filename}.{extension}")));

        strategy.visitTranslationFiles("src/test.odt", visitor,
            Optional.<String>absent());

        verify(visitor).visit(eq(deMapping), fileCaptor.capture());
        assertThat(fileCaptor.getValue(), equalTo(deTransFile));

        verify(visitor).visit(eq(zhMapping), fileCaptor.capture());
        assertThat(fileCaptor.getValue(), equalTo(zhTransFile));

        verifyNoMoreInteractions(visitor);
    }
}
