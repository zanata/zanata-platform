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
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import com.google.common.base.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.client.TestUtils.createAndAddLocaleMapping;

public class GettextPushStrategyTest {
    @Rule
    public TempTransFileRule tempFileRule = new TempTransFileRule();
    private GettextPushStrategy strategy;
    private PushOptionsImpl opts;

    @Before
    public void setUp() throws IOException {
        strategy = new GettextPushStrategy();
        opts = new PushOptionsImpl();
        opts.setProjectType("gettext");
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

        File deTransFile = strategy.getTransFile(deMapping, "po/message.pot");
        assertThat(deTransFile, Matchers.equalTo(new File(tempFileRule.getTransDir(), "po/de.po")));

        File zhTransFile = strategy.getTransFile(zhMapping, "po/message.pot");
        assertThat(zhTransFile, Matchers.equalTo(new File(tempFileRule.getTransDir(), "po/zh_Hans.po")));
    }

}