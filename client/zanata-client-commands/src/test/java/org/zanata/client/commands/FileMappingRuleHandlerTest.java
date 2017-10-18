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

import com.google.common.base.Optional;
import org.junit.Test;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.client.commands.FileMappingRuleHandler.Placeholders;
import static org.zanata.client.commands.FileMappingRuleHandler.isRuleValid;

public class FileMappingRuleHandlerTest {

    private ConfigurableProjectOptions opts = new PushOptionsImpl();

    @Test
    public void canCheckSyntaxErrorInTheRule() {
        assertThat(isRuleValid("{a")).as("unbalanced brace").isFalse();
        assertThat(isRuleValid("a}")).as("unbalanced brace").isFalse();
        assertThat(isRuleValid("a")).as("missing brace").isFalse();
        assertThat(isRuleValid("{a}")).as("invalid placeholder").isFalse();
        assertThat(isRuleValid("{path}")).as("missing mandatory placeholder")
                .isFalse();
        assertThat(isRuleValid("{path}/{locale_with_underscore}.po")).isTrue();
        assertThat(isRuleValid("{path}/../{locale}/{filename}.po")).isTrue();
    }

    @Test
    public void willReturnTransFileRelativePath() {
        assertThat(getTransFile("pot/message.pot", "fr",
                "{path}/../{locale}/{filename}.po")).isEqualTo("fr/message.po");
        assertThat(getTransFile("./message.pot", "fr",
                "{path}/{locale_with_underscore}.po")).isEqualTo("fr.po");
        assertThat(getTransFile("a/path/message.odt", "de-DE",
                "{path}/{locale_with_underscore}_{filename}.{extension}"))
                .isEqualTo("a/path/de_DE_message.odt");
    }

    @Test
    public void ifNoPatternWillUseProjectType() {
        FileMappingRuleHandler handler =
                new FileMappingRuleHandler(
                        new FileMappingRule(null,
                                "{path}/{locale_with_underscore}.po"), opts);
        assertThat(handler.getRelativeTransFilePathForSourceDoc(
                DocNameWithExt.from("message.pot"),
                new LocaleMapping("zh"), Optional.<String>absent())).isEqualTo("zh.po");
    }

    private String getTransFile(String sourceFile, String locale, String rule) {
        FileMappingRuleHandler handler = new FileMappingRuleHandler(
            new FileMappingRule("**/*", rule), opts);
        return handler.getRelativeTransFilePathForSourceDoc(
                DocNameWithExt.from(sourceFile),
                new LocaleMapping(locale), Optional.<String>absent());
    }

    @Test
    public void canGetPartsFromFullFilename() {
        EnumMap<Placeholders, String> map =
                FileMappingRuleHandler.parseToMap("foo/message.pot",
                        new LocaleMapping("zh-CN", "zh-Hans"), Optional.<String>absent());
        assertThat(map).containsEntry(Placeholders.path, "foo");
        assertThat(map).containsEntry(Placeholders.filename, "message");
        assertThat(map).containsEntry(Placeholders.extension, "pot");
        assertThat(map).containsEntry(Placeholders.locale, "zh-Hans");
        assertThat(map).containsEntry(Placeholders.localeWithUnderscore, "zh_Hans");
    }

    @Test
    public void canGetPartsFromFullFilename2() {
        EnumMap<Placeholders, String> map =
            FileMappingRuleHandler.parseToMap("foo/message.pot",
                new LocaleMapping("zh-CN", "zh-Hans"), Optional.of("po"));
        assertThat(map).containsEntry(Placeholders.path, "foo");
        assertThat(map).containsEntry(Placeholders.filename, "message");
        assertThat(map).containsEntry(Placeholders.extension, "po");
        assertThat(map).containsEntry(Placeholders.locale, "zh-Hans");
        assertThat(map).containsEntry(Placeholders.localeWithUnderscore, "zh_Hans");
    }

    @Test
    public void canTestApplicable() {
        opts.setSrcDir(new File("."));
        FileMappingRuleHandler handler = new FileMappingRuleHandler(
                new FileMappingRule("**/*.odt",
                        "{locale}/{filename}.{extension}"), opts);
        assertThat(handler.isApplicable(
                DocNameWithExt.from("test/doc.odt"))).isTrue();
        assertThat(handler.isApplicable(
                DocNameWithExt.from("test/doc.pot"))).isFalse();
        assertThat(handler.isApplicable(
                DocNameWithExt.from("doc.pot"))).isFalse();
        assertThat(handler.isApplicable(
                DocNameWithExt.from("doc.odt"))).isTrue();
    }
}
