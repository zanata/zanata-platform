package org.zanata.feature.rest;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.util.ZanataRestCaller.buildSourceResource;
import static org.zanata.util.ZanataRestCaller.buildTextFlow;
import static org.zanata.util.ZanataRestCaller.buildTextFlowTarget;
import static org.zanata.util.ZanataRestCaller.buildTranslationResource;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CopyTransTest {
    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    private boolean COPYTRANS = true;

    @Test
    @Ignore("see https://github.com/zanata/zanata-server/pull/571#issuecomment-55547577 this test can be used to reproduce that issue.")
    // see org.zanata.rest.service.ResourceUtils.transferFromTextFlows()
    public void testPushTranslationAndCopyTrans() {
        ZanataRestCaller restCaller =
                new ZanataRestCaller();
        String projectSlug = "push-test";
        String iterationSlug = "master";
        String projectType = "gettext";
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType);

        String docId = "messages";
        Resource sourceResource = buildSourceResource(docId);
        TranslationsResource transResource = buildTranslationResource();
        int numOfMessages = 520;
        for (int i = 0; i < numOfMessages; i++) {
            String resId = "res" + i;
            String content = "content" + i;
            sourceResource.getTextFlows().add(buildTextFlow(resId, content));
            transResource.getTextFlowTargets().add(
                    buildTextFlowTarget(resId, content));
        }
        restCaller.asyncPushSource(projectSlug, iterationSlug, sourceResource, false);
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId,
                new LocaleId("pl"), transResource, "import");

        assertThat(true, Matchers.is(true));

        // create another version
        restCaller.createProjectAndVersion(projectSlug, "2", projectType);
        restCaller.asyncPushSource(projectSlug, "2", sourceResource, false);
        restCaller.asyncPushTarget(projectSlug, "2", docId, new LocaleId("pl"),
                transResource, "import");

        // push to old version again
        restCaller.asyncPushSource(projectSlug, iterationSlug, sourceResource,
                COPYTRANS);
    }

    @Test
    @Ignore("see https://github.com/zanata/zanata-server/pull/571#issuecomment-56011217 this test can be used to reproduce that issue.")
    // see org.zanata.dao.TextFlowDAO.getByDocumentAndResIds
    public
            void testPushTranslationRepeatedly() {
        ZanataRestCaller restCaller =
                new ZanataRestCaller();
        String projectSlug = "push-test";
        String iterationSlug = "master";
        String projectType = "gettext";
        restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                projectType);

        String docId = "messages";
        Resource sourceResource = buildSourceResource(docId);
        TranslationsResource transResource = buildTranslationResource();
        int numOfMessages = 10;
        for (int i = 0; i < numOfMessages; i++) {
            String resId = "res" + i;
            String content = "content" + i;
            sourceResource.getTextFlows().add(buildTextFlow(resId, content));
            transResource.getTextFlowTargets().add(
                    buildTextFlowTarget(resId, content));
        }
        restCaller.asyncPushSource(projectSlug, iterationSlug, sourceResource,
                false);
        LocaleId localeId = new LocaleId("pl");
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId,
                localeId, transResource, "auto");
        restCaller.runCopyTrans(projectSlug, iterationSlug, docId);

        assertThat(true, Matchers.is(true));

        // create some obsolete text flows
        Resource updatedSource = buildSourceResource(docId);
        TranslationsResource updatedTransResource = buildTranslationResource();
        for (int i = 0; i < numOfMessages; i++) {
            String resId = "res" + i;
            String content = "content" + i + " changed";
            updatedSource.getTextFlows().add(buildTextFlow(resId, content));
            updatedTransResource.getTextFlowTargets().add(buildTextFlowTarget(resId, content));
        }

        // push updated source (same resId different content)
        restCaller.asyncPushSource(projectSlug, iterationSlug, updatedSource, false);
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId, localeId, updatedTransResource, "auto");

        // push again
        restCaller.asyncPushSource(projectSlug, iterationSlug, updatedSource, false);
        restCaller.asyncPushTarget(projectSlug, iterationSlug, docId, localeId, updatedTransResource, "auto");
    }
}
