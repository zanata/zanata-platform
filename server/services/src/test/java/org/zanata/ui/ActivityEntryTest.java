package org.zanata.ui;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.common.ActivityType;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.Activity;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.IsEntityWithType;
import org.zanata.model.type.EntityType;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.ActivityService;
import org.zanata.util.UrlUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.zanata.test.EntityTestData.makeHTextFlow;
import static org.zanata.test.EntityTestData.setId;

public class ActivityEntryTest {
    private ActivityEntry activityEntry;
    @Mock
    private DocumentDAO documentDAO;
    @Mock
    private ActivityService activityService;
    @Mock
    private UrlUtil urlUtil;
    @Mock
    private Messages msgs;
    @Mock
    private ZanataIdentity identity;
    private HProjectIteration iteration;
    private HProject project;
    private int wordCount = 10;
    private HLocale targetLocale;
    private HTextFlow textFlow;
    private HTextFlowTarget textFlowTarget;
    private HDocument document;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        activityEntry =
                new ActivityEntry(activityService, urlUtil, documentDAO, msgs,
                        identity);
        iteration = new HProjectIteration();
        setId(iteration, 1L);
        iteration.setSlug("master");
        project = new HProject();
        setId(project, 2L);
        project.setSlug("about-fedora");
        project.setName("About Fedora");
        iteration.setProject(project);
        targetLocale = new HLocale(LocaleId.DE);
        setId(targetLocale, 3L);
        textFlow = makeHTextFlow(4L, targetLocale, ContentState.Translated);
        document = textFlow.getDocument();
        setId(document, 5L);
        document.setProjectIteration(iteration);
        textFlowTarget = textFlow.getTargets().get(targetLocale.getId());
        setId(textFlowTarget, 6L);
        textFlowTarget.setLocale(targetLocale);
    }

    @Test
    public void canGetWordCountMessage() {
        assertThat(activityEntry.getWordsCountMessage(1)).isEqualTo("1 word");
        assertThat(activityEntry.getWordsCountMessage(10)).isEqualTo("10 words");
    }

    @Test
    public void canGetProjectUrlIfActivityTypeSupportsIt() {
        when(activityService.getEntity(any(EntityType.class), anyLong())).thenReturn(
                iteration);
        String expectedUrl = "http://localhost/project/about-fedora";
        when(urlUtil.projectUrl(project.getSlug())).thenReturn(expectedUrl);

        assertThat(activityEntry.getProjectUrl(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                textFlowTarget))).isEqualTo(expectedUrl);
        assertThat(activityEntry.getProjectUrl(makeActivity(ActivityType.UPDATE_TRANSLATION,
                textFlowTarget))).isEqualTo(expectedUrl);
        assertThat(activityEntry.getProjectUrl(makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                textFlowTarget))).isEqualTo(expectedUrl);
        assertThat(activityEntry.getProjectUrl(makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                textFlowTarget))).isEqualTo(expectedUrl);

    }

    @Test
    public void canGetProjectName() {
        when(activityService.getEntity(any(EntityType.class), anyLong()))
                .thenReturn(iteration);

        assertThat(activityEntry
                .getProjectName(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(project.getName());
        assertThat(activityEntry
                .getProjectName(makeActivity(ActivityType.UPDATE_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(project.getName());
        assertThat(activityEntry.getProjectName(
                makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(project.getName());
        assertThat(activityEntry.getProjectName(
                makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(project.getName());
    }

    @Test
    public void canGetVersionUrlIfActivityTypeSupportsIt() {
        when(activityService.getEntity(any(EntityType.class), anyLong()))
                .thenReturn(iteration);
        String expectedUrl = "http://localhost/project/about-fedora/master";
        when(urlUtil.versionUrl(project.getSlug(), iteration.getSlug())).thenReturn(expectedUrl);

        assertThat(activityEntry
                .getVersionUrl(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
        assertThat(activityEntry
                .getVersionUrl(makeActivity(ActivityType.UPDATE_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
        assertThat(activityEntry.getVersionUrl(
                makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
        assertThat(activityEntry.getVersionUrl(
                makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
    }

    @Test
    public void canGetVersionName() {
        when(activityService.getEntity(any(EntityType.class), anyLong()))
                .thenReturn(iteration);

        assertThat(activityEntry
                .getVersionName(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(iteration.getSlug());
        assertThat(activityEntry
                .getVersionName(makeActivity(ActivityType.UPDATE_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(iteration.getSlug());
        assertThat(activityEntry.getVersionName(
                makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(iteration.getSlug());
        assertThat(activityEntry.getVersionName(
                makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(iteration.getSlug());
    }

    @Test
    public void canGetEditorUrlIfItIsTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);
        String expectedUrl = "http://localhost/goes/to/editor";
        when(urlUtil.editorTransUnitUrl(project.getSlug(), iteration.getSlug(), targetLocale.getLocaleId(), textFlow.getLocale(), document.getDocId(), textFlow.getId())).thenReturn(expectedUrl);

        assertThat(activityEntry
                .getEditorUrl(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
        assertThat(activityEntry
                .getEditorUrl(makeActivity(ActivityType.UPDATE_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
    }

    @Test
    public void willNotGetEditorUrlIfItIsSourceUpload() {
        when(activityService.getEntity(any(EntityType.class), anyLong()))
                .thenReturn(iteration);
        assertThat(activityEntry
                .getEditorUrl(makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo("");
    }

    @Test
    public void canGetEditorUrlIfItIsTranslationDocumentUploadAndLastTargetIsAvailable() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HDocument), anyLong()))
                .thenReturn(document);
        when(documentDAO.getLastTranslatedTargetOrNull(document.getId())).thenReturn(textFlowTarget);
        String expectedUrl = "http://localhost/goes/to/editor";
        when(urlUtil.editorTransUnitUrl(project.getSlug(), iteration.getSlug(), targetLocale.getLocaleId(), textFlow.getLocale(), document.getDocId(), textFlow.getId())).thenReturn(expectedUrl);

        assertThat(activityEntry
                .getEditorUrl(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo(expectedUrl);
    }

    @Test
    public void willNotGetEditorUrlIfTranslationDocumentUploadButNoLastTargetIsAvailable() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HDocument), anyLong()))
                .thenReturn(document);
        when(documentDAO.getLastTranslatedTargetOrNull(document.getId())).thenReturn(null);

        assertThat(activityEntry
                .getEditorUrl(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo("");
    }

    @Test
    public void canGetEditorDocumentUrlIfItIsTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);
        String expectedUrl = "http://localhost/goes/to/editor";
        when(urlUtil.editorDocumentUrl(project.getSlug(), iteration.getSlug(), targetLocale.getLocaleId(), textFlow.getLocale(), document.getDocId())).thenReturn(
                expectedUrl);

        assertThat(activityEntry
                .getDocumentUrl(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
        assertThat(activityEntry
                .getDocumentUrl(makeActivity(ActivityType.UPDATE_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
    }

    @Test
    public void canGetSourceFilesViewUrlIfItIsSourceUpload() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);
        String expectedUrl = "http://localhost/goes/to/source/list";
        when(urlUtil.sourceFilesViewUrl(project.getSlug(), iteration.getSlug())).thenReturn(expectedUrl);

        assertThat(activityEntry
                .getDocumentUrl(makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
    }

    @Test
    public void canGetDocumentUrlIfItIsTranslationDocumentUploadAndLastTargetIsAvailable() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HDocument), anyLong()))
                .thenReturn(document);
        when(documentDAO.getLastTranslatedTargetOrNull(document.getId())).thenReturn(textFlowTarget);
        String expectedUrl = "http://localhost/goes/to/editor";
        when(urlUtil.editorDocumentUrl(project.getSlug(), iteration.getSlug(), targetLocale.getLocaleId(), textFlow.getLocale(), document.getDocId())).thenReturn(
                expectedUrl);

        assertThat(activityEntry
                .getDocumentUrl(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo(expectedUrl);
    }

    @Test
    public void willNotGetDocumentUrlIfTranslationDocumentUploadButNoLastTargetIsAvailable() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HDocument), anyLong()))
                .thenReturn(document);
        when(documentDAO.getLastTranslatedTargetOrNull(document.getId())).thenReturn(null);

        assertThat(activityEntry
                .getDocumentUrl(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo("");
    }

    @Test
    public void canGetDocumentListUrlIfItIsTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);
        String expectedUrl = "http://localhost/goes/to/document/list";
        when(urlUtil.editorDocumentListUrl(project.getSlug(),
                iteration.getSlug(), targetLocale.getLocaleId(),
                textFlow.getLocale(), false)).thenReturn(
                expectedUrl);

        assertThat(activityEntry
                .getDocumentListUrl(
                        makeActivity(ActivityType.REVIEWED_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(expectedUrl);
        assertThat(activityEntry
                .getDocumentListUrl(
                        makeActivity(ActivityType.UPDATE_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(expectedUrl);
    }

    @Test
    public void willNotGetDocumentListUrlIfItIsSourceUpload() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        assertThat(activityEntry
                .getDocumentListUrl(
                        makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                                textFlowTarget)))
                .isEqualTo("");
    }

    @Test
    public void canGetDocumentListUrlIfItIsTranslationDocumentUploadAndLastTargetIsAvailable() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HDocument), anyLong()))
                .thenReturn(document);
        when(documentDAO.getLastTranslatedTargetOrNull(document.getId())).thenReturn(textFlowTarget);
        String expectedUrl = "http://localhost/goes/to/editor";
        when(urlUtil.editorDocumentListUrl(project.getSlug(),
                iteration.getSlug(), targetLocale.getLocaleId(),
                textFlow.getLocale(), false)).thenReturn(
                expectedUrl);

        assertThat(activityEntry
                .getDocumentListUrl(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo(expectedUrl);
    }

    @Test
    public void willNotGetDocumentListUrlIfTranslationDocumentUploadButNoLastTargetIsAvailable() {
        when(activityService.getEntity(eq(EntityType.HProjectIteration), anyLong()))
                .thenReturn(iteration);
        when(activityService.getEntity(eq(EntityType.HDocument), anyLong()))
                .thenReturn(document);
        when(documentDAO.getLastTranslatedTargetOrNull(document.getId())).thenReturn(null);

        assertThat(activityEntry
                .getDocumentListUrl(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo("");
    }

    @Test
    public void canGetDocumentNameIfItIsTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        assertThat(activityEntry
                .getDocumentName(
                        makeActivity(ActivityType.REVIEWED_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(document.getName());
        assertThat(activityEntry
                .getDocumentName(
                        makeActivity(ActivityType.UPDATE_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(document.getName());
    }

    @Test
    public void canGetDocumentNameIfItIsDocumentUpload() {
        when(activityService.getEntity(any(EntityType.class), anyLong()))
                .thenReturn(document);

        assertThat(activityEntry
                .getDocumentName(
                        makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                                document)))
                .isEqualTo(document.getName());
        assertThat(activityEntry
                .getDocumentName(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo(document.getName());
    }

    @Test
    public void canGetLanguageNameIfItIsTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        assertThat(activityEntry
                .getLanguageName(
                        makeActivity(ActivityType.REVIEWED_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(textFlowTarget.getLocaleId().getId());
        assertThat(activityEntry
                .getLanguageName(
                        makeActivity(ActivityType.UPDATE_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(textFlowTarget.getLocaleId().getId());
    }

    @Test
    public void willNotGetLanguageNameIfItIsSourceUpload() {
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        assertThat(activityEntry
                .getLanguageName(
                        makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                                textFlowTarget)))
                .isEqualTo("");
    }

    @Test
    public void canGetLanguageNameIfItIsTranslationDocumentUploadAndLastTargetIsAvailable() {
        when(activityService.getEntity(eq(EntityType.HDocument), anyLong()))
                .thenReturn(document);
        when(documentDAO.getLastTranslatedTargetOrNull(document.getId())).thenReturn(textFlowTarget);

        assertThat(activityEntry
                .getLanguageName(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo(textFlowTarget.getLocaleId().getId());
    }

    @Test
    public void willNotGetLanguageNameIfTranslationDocumentUploadButNoLastTargetIsAvailable() {
        when(activityService.getEntity(eq(EntityType.HDocument), anyLong()))
                .thenReturn(document);
        when(documentDAO.getLastTranslatedTargetOrNull(document.getId())).thenReturn(null);

        assertThat(activityEntry
                .getDocumentListUrl(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo("");
    }

    @Test
    public void canGetLastTextFlowContentIfItIsTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        assertThat(activityEntry
                .getLastTextFlowContent(
                        makeActivity(ActivityType.REVIEWED_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(textFlow.getContents().get(0));
        assertThat(activityEntry
                .getLastTextFlowContent(
                        makeActivity(ActivityType.UPDATE_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(textFlow.getContents().get(0));
    }

    @Test
    public void willNotGetLastTextFlowContentIfItIsNotTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        assertThat(activityEntry
                .getLastTextFlowContent(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                textFlowTarget))).isEqualTo("");
        assertThat(activityEntry
                .getLastTextFlowContent(
                        makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                                textFlowTarget))).isEqualTo("");
    }

    @Test
    public void getActivityMessage() {
        testGetActivityMessage(true);
        testGetActivityMessage(false);
    }

    private void testGetActivityMessage(boolean canView) {
        HTextFlow textFlow = new HTextFlow();
        HTextFlowTarget target = Mockito.mock(HTextFlowTarget.class);
        when(target.getId()).thenReturn(1L);
        when(target.getTextFlow()).thenReturn(textFlow);

        when(activityService.getEntity(EntityType.HProjectIteration, 1L))
                .thenReturn(iteration);
        when(activityService.getEntity(EntityType.HTexFlowTarget, 1L))
                .thenReturn(target);
        when(identity.hasPermission(iteration, "read")).thenReturn(canView);
        when(msgs.format(any(), any(), any(), any())).thenReturn(String.valueOf(canView));
        when(msgs.format(any(), any(), any())).thenReturn(String.valueOf(canView));

        Activity activity =
                makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT, target);
        assertThat(activityEntry.getActivityMessage(activity)).isEqualTo(String.valueOf(canView));
    }

    @Test
    public void canViewProject() {
        testCanViewProject(true);
        testCanViewProject(false);
    }

    private void testCanViewProject(boolean canView) {
        when(activityService.getEntity(EntityType.HProjectIteration, 1L))
                .thenReturn(iteration);
        when(identity.hasPermission(iteration, "read")).thenReturn(canView);

        Activity activity =
                makeActivity(ActivityType.UPDATE_TRANSLATION, iteration);
        assertThat(activityEntry.canViewProject(activity)).isEqualTo(canView);
    }


    private Activity makeActivity(ActivityType activityType,
            IsEntityWithType target) {
        return new Activity(new HPerson(), iteration, target,
                activityType, wordCount);
    }
}
