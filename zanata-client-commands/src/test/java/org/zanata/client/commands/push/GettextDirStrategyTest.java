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

import java.io.File;
import java.io.IOException;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.zanata.client.TempTransFileRule;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import com.google.common.base.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.zanata.client.TestUtils.createAndAddLocaleMapping;

public class GettextDirStrategyTest {
    @Rule
    public TempTransFileRule tempFileRule = new TempTransFileRule();
    private GettextDirStrategy strategy;
    private PushOptionsImpl opts;

    @Before
    public void setUp() throws IOException {
        strategy = new GettextDirStrategy();
        opts = new PushOptionsImpl();
        opts.setProjectType("podir");
        opts.setTransDir(tempFileRule.getTransDir());
        opts.setLocaleMapList(new LocaleList());
        strategy.setPushOptions(opts);
    }

    @Test
    public void willWorkWithoutMappingRules() throws IOException {
        LocaleMapping deMapping = createAndAddLocaleMapping("de",
                Optional.<String> absent(), opts);
        LocaleMapping zhMapping =
                createAndAddLocaleMapping("zh-CN",
                        Optional.of("zh-Hans"),
                        opts);

        File deTransFile = strategy.getTransFile(deMapping, "foo/message");
        assertThat(deTransFile, Matchers.equalTo(new File(tempFileRule
                .getTransDir(), "de/foo/message.po")));

        File zhTransFile = strategy.getTransFile(zhMapping, "foo/message");
        assertThat(zhTransFile, Matchers.equalTo(new File(tempFileRule
                .getTransDir(), "zh-Hans/foo/message.po")));
    }

    @Test
    public void willWorkWithMappingRules() {
        opts.setFileMappingRules(newArrayList(
                new FileMappingRule(null, "{locale}/{path}/{filename}.po")));
        LocaleMapping deMapping = createAndAddLocaleMapping("de",
                Optional.<String> absent(), opts);
        LocaleMapping zhMapping =
                createAndAddLocaleMapping("zh-CN",
                        Optional.of("zh-Hans"),
                        opts);

        File deTransFile = strategy.getTransFile(deMapping, "foo/message");
        assertThat(deTransFile, Matchers.equalTo(new File(tempFileRule
                .getTransDir(), "de/foo/message.po")));

        File zhTransFile = strategy.getTransFile(zhMapping, "foo/message");
        assertThat(zhTransFile, Matchers.equalTo(new File(tempFileRule
                .getTransDir(), "zh-Hans/foo/message.po")));
    }

}