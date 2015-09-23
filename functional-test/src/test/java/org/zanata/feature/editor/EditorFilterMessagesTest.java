/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.feature.editor;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.ZanataRestCaller.buildSourceResource;
import static org.zanata.util.ZanataRestCaller.buildTextFlow;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditorFilterMessagesTest extends ZanataTestCase {
    private final String document = "messages";

    @Before
    public void setUp() {
        ZanataRestCaller restCaller = new ZanataRestCaller();
        Resource sourceResource =
                buildSourceResource(document,
                        buildTextFlow("res1", "hello world"),
                        buildTextFlow("res2", "greetings"),
                        buildTextFlow("res3", "hey"));
        restCaller.postSourceDocResource("about-fedora", "master",
                sourceResource, false);
        new LoginWorkFlow().signIn("admin", "admin");
    }

    @Feature(summary = "The user can filter translation entries using more " +
            "than one search term",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void canFilterByMultipleFields() {
        EditorPage editorPage = new BasicWorkFlow()
                .goToEditor("about-fedora", "master", "fr", document);

        assertThat(editorPage.getMessageSources()).containsExactly(
                "hello world", "greetings", "hey");

        final EditorPage page = editorPage.inputFilterQuery("resource-id:res2");
        page.waitForPageSilence();
        assertThat(page.getMessageSources()).contains("greetings");
    }

    @Feature(summary = "The user may save the filter url for later use",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void editorFilterIsBookmarkable() {
        String urlForEditor =
                String.format(BasicWorkFlow.EDITOR_TEMPLATE, "about-fedora",
                        "master", "fr", document);
        String urlWithFilterCondition =
                urlForEditor + ";search:hello%20w;resid:res1";
        EditorPage editorPage =
                new BasicWorkFlow().goToPage(urlWithFilterCondition,
                        EditorPage.class);

        assertThat(editorPage.getMessageSources()).containsExactly("hello world");
        assertThat(editorPage.getFilterQuery().trim()).isEqualTo("text:hello w resource-id:res1");
    }
}
