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

package org.zanata.client.commands.pull;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.TempTransFileRule;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleMapping;

import com.google.common.collect.Lists;

public class RawPullStrategyTest {
    @Rule
    public TempTransFileRule tempTransFileRule = new TempTransFileRule();
    private RawPullStrategy strategy;
    private PullOptionsImpl opts;
    @Mock
    private InputStream transFile;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        strategy = new RawPullStrategy();
        opts = new PullOptionsImpl();
        opts.setTransDir(tempTransFileRule.getTransDir());
        opts.setProjectType("file");
        strategy.setPullOptions(opts);
    }

    @Test
    public void canWriteToFileWithoutMapping() throws IOException {
        when(transFile.read(any(byte[].class))).thenReturn(-1);

        strategy.writeTransFile("foo/test.odt", new LocaleMapping("de"),
                transFile);

        assertThat(
                new File(tempTransFileRule.getTransDir(), "de/foo/test.odt").exists(),
                is(true));
    }

    @Test
    public void canWriteToFileWithMapping() throws IOException {
        opts.setFileMappingRules(Lists.newArrayList(new FileMappingRule(
                "{locale}/{filename}.{extension}")));
        when(transFile.read(any(byte[].class))).thenReturn(-1);

        strategy.writeTransFile("foo/test.odt", new LocaleMapping("de"),
                transFile);

        assertThat(
                new File(tempTransFileRule.getTransDir(), "de/test.odt").exists(),
                is(true));
    }

}
