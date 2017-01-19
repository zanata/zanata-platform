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
package org.zanata.feature.concurrentedit;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.common.LocaleId;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.webtrans.EditorPage;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.ZanataRestCaller.*;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ConcurrentEditTest extends ZanataTestCase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ConcurrentEditTest.class);

    private ZanataRestCaller restCaller;

    @Before
    public void setUp() {
        restCaller = new ZanataRestCaller();
    }

    @Feature(
            summary = "The system will propagate translations done by upload and copyTrans to editor",
            bugzilla = 1067253)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void editorReceivesRestServiceResults() {
        // create project and push source
        String projectSlug = "base";
        String iterationSlug = "master";
        String projectType = "gettext";
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType);
        String docId = "test.pot";
        Resource sourceResource =
                buildSourceResource(docId, buildTextFlow("res1", "hello world"),
                        buildTextFlow("res2", "greetings"));
        restCaller.postSourceDocResource(projectSlug, iterationSlug,
                sourceResource, false);
        // open editor
        new LoginWorkFlow().signIn("admin", "admin");
        // webTrans
        final EditorPage editorPage = new BasicWorkFlow().goToEditor("base",
                "master", "pl", "test.pot");
        String translation = editorPage.getMessageTargetAtRowIndex(0);
        // for some reason getText() will return one space in it
        assertThat(translation.trim()).isEmpty();
        // push target
        TranslationsResource translationsResource = buildTranslationResource(
                buildTextFlowTarget("res1", "hello world translated"));
        restCaller.postTargetDocResource(projectSlug, iterationSlug, docId,
                new LocaleId("pl"), translationsResource, "auto");
        // REST push broadcast event to editor
        assertThat(editorPage.expectBasicTranslationAtRowIndex(0,
                "hello world translated")).isTrue();
    }

    @Feature(
            summary = "The system will show concurrently changed translations to the web editor user",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void editorReceivesCopyTransResults() throws Exception {
        // create project and populate master version
        String projectSlug = "base";
        String iterationSlug = "master";
        String projectType = "gettext";
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType);
        String docId = "test.pot";
        Resource sourceResource =
                buildSourceResource(docId, buildTextFlow("res1", "hello world"),
                        buildTextFlow("res2", "greetings"));
        restCaller.postSourceDocResource(projectSlug, iterationSlug,
                sourceResource, false);
        TranslationsResource translationsResource = buildTranslationResource(
                buildTextFlowTarget("res1", "hello world translated"));
        restCaller.postTargetDocResource(projectSlug, iterationSlug, docId,
                new LocaleId("pl"), translationsResource, "auto");
        // create and push source but disable copyTrans
        restCaller.createProjectAndVersion(projectSlug, "beta", projectType);
        restCaller.postSourceDocResource(projectSlug, "beta", sourceResource,
                false);
        // open editor
        new LoginWorkFlow().signIn("admin", "admin");
        // webTrans
        final EditorPage editorPage = new BasicWorkFlow().goToEditor("base",
                "beta", "pl", "test.pot");
        String translation = editorPage.getMessageTargetAtRowIndex(0);
        // for some reason getText() will return one space in it
        assertThat(translation.trim()).isEmpty();
        // run copyTrans
        restCaller.runCopyTrans(projectSlug, "beta", docId);
        // copyTrans broadcast event to editor
        assertThat(editorPage.expectBasicTranslationAtRowIndex(0,
                "hello world translated")).isTrue();
    }
}
