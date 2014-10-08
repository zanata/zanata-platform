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

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.zanata.client.TestUtils.createAndAddLocaleMapping;

import java.io.File;
import java.io.IOException;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.adapter.xliff.XliffReader;
import org.zanata.client.TempTransFileRule;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class XliffStrategyTest {

    @Rule
    public TempTransFileRule tempFileRule = new TempTransFileRule();
    private XliffStrategy strategy;
    private PushOptionsImpl opts;
    @Captor
    private ArgumentCaptor<TranslationsResource> transResourceCaptor;
    @Mock
    private PushCommand.TranslationResourcesVisitor visitor;
    private Resource sourceResource;
    @Mock
    private XliffReader reader;
    @Captor
    private ArgumentCaptor<File> fileCapture;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        strategy = new XliffStrategy(reader);
        opts = new PushOptionsImpl();
        opts.setLocaleMapList(new LocaleList());
        opts.setTransDir(tempFileRule.getTransDir());
        opts.setProjectType("xliff");
        strategy.setPushOptions(opts);

        sourceResource = new Resource("test");
    }

    @Test
    public void canVisitTranslationFileWithoutFileMapping() throws Exception {
        // with translation
        LocaleMapping deMapping = createAndAddLocaleMapping("de",
                Optional.<String> absent(), opts);
        // with translation and has map-from
        LocaleMapping zhMapping =
                createAndAddLocaleMapping("zh-CN",
                        Optional.of("zh-Hans"),
                        opts);
        // no translation
        opts.getLocaleMapList().add(new LocaleMapping("ja"));

        File deTransFile =
                tempFileRule.createTransFileRelativeToTransDir(
                        "foo/message_de.xml");
        File zhTransFile =
                tempFileRule.createTransFileRelativeToTransDir(
                        "foo/message_zh_Hans.xml");

        strategy.visitTranslationResources("foo/message",
                sourceResource, visitor);

        verify(visitor).visit(eq(deMapping), transResourceCaptor.capture());
        verify(visitor).visit(eq(zhMapping), transResourceCaptor.capture());
        verify(reader, times(2)).extractTarget(fileCapture.capture());
        assertThat(fileCapture.getAllValues(),
                Matchers.contains(deTransFile, zhTransFile));

        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void canVisitTranslationFileUsingFileMapping() throws Exception {
        // with translation
        LocaleMapping deMapping = createAndAddLocaleMapping("de",
                Optional.<String> absent(), opts);
        // with translation and has map-from
        LocaleMapping zhMapping =
                createAndAddLocaleMapping("zh-CN",
                        Optional.of("zh-Hans"),
                        opts);
        // no translation
        opts.getLocaleMapList().add(new LocaleMapping("ja"));

        File deTransFile =
                tempFileRule.createTransFileRelativeToTransDir(
                        "foo/message_de.xml");
        File zhTransFile =
                tempFileRule.createTransFileRelativeToTransDir(
                        "foo/message_zh_Hans.xml");

        opts.setFileMappingRules(Lists.newArrayList(new FileMappingRule(
                "{path}/{filename}_{locale_with_underscore}.{extension}")));

        strategy.visitTranslationResources("foo/message",
                sourceResource, visitor);

        verify(visitor).visit(eq(deMapping), transResourceCaptor.capture());
        verify(visitor).visit(eq(zhMapping), transResourceCaptor.capture());
        verify(reader, times(2)).extractTarget(fileCapture.capture());
        assertThat(fileCapture.getAllValues(),
                Matchers.contains(deTransFile, zhTransFile));

        verifyNoMoreInteractions(visitor);
    }

}
