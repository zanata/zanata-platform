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
package org.zanata.feature.editor

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.page.webtrans.EditorPage
import org.zanata.util.ZanataRestCaller
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow

import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.ZanataRestCaller.buildSourceResource
import org.zanata.util.ZanataRestCaller.buildTextFlow

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@DetailedTest
class EditorFilterMessagesTest : ZanataTestCase() {
    private val document = "messages"

    @BeforeEach
    fun setUp() {
        val restCaller = ZanataRestCaller()
        val sourceResource = buildSourceResource(document,
                buildTextFlow("res1", "hello world"),
                buildTextFlow("res2", "greetings"),
                buildTextFlow("res3", "hey"))
        restCaller.postSourceDocResource("about-fedora", "master",
                sourceResource, false)
        LoginWorkFlow().signIn("admin", "admin")
    }

    @Trace(summary = "The user can filter translation entries using more " +
            "than one search term")
    @Test
    fun canFilterByMultipleFields() {
        val editorPage = BasicWorkFlow()
                .goToEditor("about-fedora", "master", "fr", document)

        assertThat(editorPage.messageSources)
                .containsExactly("hello world", "greetings", "hey")

        val page = editorPage.inputFilterQuery("resource-id:res2")
        page.waitForPageSilence()
        assertThat(page.messageSources).contains("greetings")
    }

    @Trace(summary = "The user may save the filter url for later use")
    @Test
    fun editorFilterIsBookmarkable() {
        val urlForEditor = String.format(BasicWorkFlow.EDITOR_TEMPLATE, "about-fedora",
                "master", "fr", document)
        val urlWithFilterCondition = "$urlForEditor;search:hello%20w;resid:res1"
        val editorPage = BasicWorkFlow().goToPage(urlWithFilterCondition,
                EditorPage::class.java)

        assertThat(editorPage.messageSources).containsExactly("hello world")
        assertThat(editorPage.filterQuery)
                .isEqualTo("text:hello w resource-id:res1 ")
    }
}
