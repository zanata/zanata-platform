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

package org.zanata.client.commands;

import java.io.File;
import java.util.EnumMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.zanata.client.commands.FileMappingRuleHandler.*;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.ProjectType;

import com.google.common.base.Optional;

public class FileMappingRuleHandlerTest {

    private ConfigurableProjectOptions opts = new PushOptionsImpl();

    @Test
    public void canCheckSyntaxErrorInTheRule() {
        assertThat("unbalanced brace", isRuleValid("{a"), equalTo(false));
        assertThat("unbalanced brace", isRuleValid("a}"), equalTo(false));
        assertThat("missing brace", isRuleValid("a"), equalTo(false));
        assertThat("invalid placeholder",
                isRuleValid("{a}"), equalTo(false));
        assertThat("missing mandatory placeholder",
                isRuleValid("{path}"), equalTo(false));
        assertThat(isRuleValid(
                "{path}/{locale_with_underscore}.po"), equalTo(true));
        assertThat(isRuleValid(
                "{path}/../{locale}/{filename}.po"), equalTo(true));
    }

    @Test
    public void willReturnTransFileRelativePath() {
        assertThat(getTransFile("pot/message.pot", "fr",
                "{path}/../{locale}/{filename}.po", ProjectType.Podir),
                Matchers.equalTo("fr/message.po"));
        assertThat(getTransFile("./message.pot", "fr",
                "{path}/{locale_with_underscore}.po", ProjectType.Gettext),
                Matchers.equalTo("fr.po"));
        assertThat(getTransFile("a/path/message.odt", "de-DE",
                "{path}/{locale_with_underscore}_{filename}.{extension}",
                        ProjectType.File),
                Matchers.equalTo("a/path/de_DE_message.odt"));
    }

    @Test
    public void ifNoPatternWillUseProjectType() {
        FileMappingRuleHandler handler =
                new FileMappingRuleHandler(
                        new FileMappingRule(null,
                                "{path}/{locale_with_underscore}.po"),
                        ProjectType.Gettext, opts);
        assertThat(handler.getRelativeTransFilePathForSourceDoc(
                DocNameWithExt.from("message.pot"),
                new LocaleMapping("zh"), Optional.<String>absent()), Matchers.equalTo("zh.po"));
    }

    private String getTransFile(String sourceFile, String locale, String rule,
            ProjectType projectType) {
        FileMappingRuleHandler handler = new FileMappingRuleHandler(
            new FileMappingRule("**/*", rule), projectType, opts);
        return handler.getRelativeTransFilePathForSourceDoc(
                DocNameWithExt.from(sourceFile),
                new LocaleMapping(locale), Optional.<String>absent());
    }

    @Test
    public void canGetPartsFromFullFilename() {
        EnumMap<Placeholders, String> map =
                FileMappingRuleHandler.parseToMap("foo/message.pot",
                        new LocaleMapping("zh-CN", "zh-Hans"), Optional.<String>absent());
        assertThat(map, Matchers.hasEntry(Placeholders.path, "foo"));
        assertThat(map, Matchers.hasEntry(Placeholders.filename, "message"));
        assertThat(map, Matchers.hasEntry(Placeholders.extension, "pot"));
        assertThat(map, Matchers.hasEntry(Placeholders.locale, "zh-Hans"));
        assertThat(map, Matchers.hasEntry(Placeholders.localeWithUnderscore, "zh_Hans"));
    }

    @Test
    public void canGetPartsFromFullFilename2() {
        EnumMap<Placeholders, String> map =
            FileMappingRuleHandler.parseToMap("foo/message.pot",
                new LocaleMapping("zh-CN", "zh-Hans"), Optional.of("po"));
        assertThat(map, Matchers.hasEntry(Placeholders.path, "foo"));
        assertThat(map, Matchers.hasEntry(Placeholders.filename, "message"));
        assertThat(map, Matchers.hasEntry(Placeholders.extension, "po"));
        assertThat(map, Matchers.hasEntry(Placeholders.locale, "zh-Hans"));
        assertThat(map, Matchers.hasEntry(Placeholders.localeWithUnderscore, "zh_Hans"));
    }

    @Test
    public void canTestApplicable() {
        opts.setSrcDir(new File("."));
        FileMappingRuleHandler handler = new FileMappingRuleHandler(
                new FileMappingRule("**/*.odt",
                        "{locale}/{filename}.{extension}"), ProjectType.File,
                opts);
        assertThat(handler.isApplicable(
                DocNameWithExt.from("test/doc.odt")), equalTo(true));
        assertThat(handler.isApplicable(
                DocNameWithExt.from("test/doc.pot")), equalTo(false));
        assertThat(handler.isApplicable(
                DocNameWithExt.from("doc.pot")), equalTo(false));
        assertThat(handler.isApplicable(
                DocNameWithExt.from("doc.odt")), equalTo(true));
    }
}
