package org.zanata.ui;

import org.assertj.core.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ActivityType;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
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
import org.zanata.service.ActivityService;
import org.zanata.util.UrlUtil;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.zanata.model.TestFixture.makeHTextFlow;

@Test(groups = "unit-tests")
public class ActivityEntryTest {
    private ActivityEntry activityEntry;
    @Mock
    private DocumentDAO documentDAO;
    @Mock
    private ActivityService activityService;
    @Mock
    private UrlUtil urlUtil;
    private HProjectIteration iteration;
    private HProject project;
    private int wordCount = 10;
    private HLocale targetLocale;
    private HTextFlow textFlow;
    private HTextFlowTarget textFlowTarget;
    private HDocument document;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        activityEntry = new ActivityEntry(activityService, urlUtil, documentDAO);
        iteration = new HProjectIteration();
        iteration.setId(1L);
        iteration.setSlug("master");
        project = new HProject();
        project.setId(2L);
        project.setSlug("about-fedora");
        project.setName("About Fedora");
        iteration.setProject(project);
        targetLocale = new HLocale(LocaleId.DE);
        targetLocale.setId(3L);
        textFlow = makeHTextFlow(4L, targetLocale, ContentState.Translated);
        document = textFlow.getDocument();
        document.setId(5L);
        document.setProjectIteration(iteration);
        textFlowTarget = textFlow.getTargets().get(targetLocale.getId());
        textFlowTarget.setId(6L);
        textFlowTarget.setLocale(targetLocale);
    }

    @Test
    public void canGetWordCountMessage() {
        Assertions.assertThat(activityEntry.getWordsCountMessage(1)).isEqualTo("1 word");
        Assertions.assertThat(activityEntry.getWordsCountMessage(10)).isEqualTo("10 words");
    }

    @Test
    public void canGetProjectUrlIfActivityTypeSupportsIt() {
        when(activityService.getEntity(any(EntityType.class), anyLong())).thenReturn(
                iteration);
        String expectedUrl = "http://localhost/project/about-fedora";
        when(urlUtil.projectUrl(project.getSlug())).thenReturn(expectedUrl);

        Assertions.assertThat(activityEntry.getProjectUrl(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                textFlowTarget))).isEqualTo(expectedUrl);
        Assertions.assertThat(activityEntry.getProjectUrl(makeActivity(ActivityType.UPDATE_TRANSLATION,
                textFlowTarget))).isEqualTo(expectedUrl);
        Assertions.assertThat(activityEntry.getProjectUrl(makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                textFlowTarget))).isEqualTo(expectedUrl);
        Assertions.assertThat(activityEntry.getProjectUrl(makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                textFlowTarget))).isEqualTo(expectedUrl);

    }

    @Test
    public void canGetProjectName() {
        when(activityService.getEntity(any(EntityType.class), anyLong()))
                .thenReturn(iteration);

        Assertions.assertThat(activityEntry
                .getProjectName(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(project.getName());
        Assertions.assertThat(activityEntry
                .getProjectName(makeActivity(ActivityType.UPDATE_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(project.getName());
        Assertions.assertThat(activityEntry.getProjectName(
                makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(project.getName());
        Assertions.assertThat(activityEntry.getProjectName(
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

        Assertions.assertThat(activityEntry
                .getVersionUrl(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
        Assertions.assertThat(activityEntry
                .getVersionUrl(makeActivity(ActivityType.UPDATE_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
        Assertions.assertThat(activityEntry.getVersionUrl(
                makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
        Assertions.assertThat(activityEntry.getVersionUrl(
                makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
    }

    @Test
    public void canGetVersionName() {
        when(activityService.getEntity(any(EntityType.class), anyLong()))
                .thenReturn(iteration);

        Assertions.assertThat(activityEntry
                .getVersionName(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(iteration.getSlug());
        Assertions.assertThat(activityEntry
                .getVersionName(makeActivity(ActivityType.UPDATE_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(iteration.getSlug());
        Assertions.assertThat(activityEntry.getVersionName(
                makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                        textFlowTarget)))
                .isEqualTo(iteration.getSlug());
        Assertions.assertThat(activityEntry.getVersionName(
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

        Assertions.assertThat(activityEntry
                .getEditorUrl(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
        Assertions.assertThat(activityEntry
                .getEditorUrl(makeActivity(ActivityType.UPDATE_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
    }

    @Test
    public void willNotGetEditorUrlIfItIsSourceUpload() {
        when(activityService.getEntity(any(EntityType.class), anyLong()))
                .thenReturn(iteration);
        Assertions.assertThat(activityEntry
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

        Assertions.assertThat(activityEntry
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

        Assertions.assertThat(activityEntry
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

        Assertions.assertThat(activityEntry
                .getDocumentUrl(makeActivity(ActivityType.REVIEWED_TRANSLATION,
                        textFlowTarget)))
                .isEqualTo(expectedUrl);
        Assertions.assertThat(activityEntry
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

        Assertions.assertThat(activityEntry
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

        Assertions.assertThat(activityEntry
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

        Assertions.assertThat(activityEntry
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
                textFlow.getLocale())).thenReturn(
                expectedUrl);

        Assertions.assertThat(activityEntry
                .getDocumentListUrl(
                        makeActivity(ActivityType.REVIEWED_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(expectedUrl);
        Assertions.assertThat(activityEntry
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

        Assertions.assertThat(activityEntry
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
                textFlow.getLocale())).thenReturn(
                expectedUrl);

        Assertions.assertThat(activityEntry
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

        Assertions.assertThat(activityEntry
                .getDocumentListUrl(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo("");
    }

    @Test
    public void canGetDocumentNameIfItIsTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        Assertions.assertThat(activityEntry
                .getDocumentName(
                        makeActivity(ActivityType.REVIEWED_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(document.getName());
        Assertions.assertThat(activityEntry
                .getDocumentName(
                        makeActivity(ActivityType.UPDATE_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(document.getName());
    }

    @Test
    public void canGetDocumentNameIfItIsDocumentUpload() {
        when(activityService.getEntity(any(EntityType.class), anyLong()))
                .thenReturn(document);

        Assertions.assertThat(activityEntry
                .getDocumentName(
                        makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                                document)))
                .isEqualTo(document.getName());
        Assertions.assertThat(activityEntry
                .getDocumentName(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo(document.getName());
    }

    @Test
    public void canGetLanguageNameIfItIsTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        Assertions.assertThat(activityEntry
                .getLanguageName(
                        makeActivity(ActivityType.REVIEWED_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(textFlowTarget.getLocaleId().getId());
        Assertions.assertThat(activityEntry
                .getLanguageName(
                        makeActivity(ActivityType.UPDATE_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(textFlowTarget.getLocaleId().getId());
    }

    @Test
    public void willNotGetLanguageNameIfItIsSourceUpload() {
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        Assertions.assertThat(activityEntry
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

        Assertions.assertThat(activityEntry
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

        Assertions.assertThat(activityEntry
                .getDocumentListUrl(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                document)))
                .isEqualTo("");
    }

    @Test
    public void canGetLastTextFlowContentIfItIsTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        Assertions.assertThat(activityEntry
                .getLastTextFlowContent(
                        makeActivity(ActivityType.REVIEWED_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(textFlow.getContent());
        Assertions.assertThat(activityEntry
                .getLastTextFlowContent(
                        makeActivity(ActivityType.UPDATE_TRANSLATION,
                                textFlowTarget)))
                .isEqualTo(textFlow.getContent());
    }

    @Test
    public void willNotGetLastTextFlowContentIfItIsNotTranslationUpdateTypes() {
        when(activityService.getEntity(eq(EntityType.HTexFlowTarget), anyLong()))
                .thenReturn(textFlowTarget);

        Assertions.assertThat(activityEntry
                .getLastTextFlowContent(
                        makeActivity(ActivityType.UPLOAD_TRANSLATION_DOCUMENT,
                                textFlowTarget)))
                .isEqualTo("");
        Assertions.assertThat(activityEntry
                .getLastTextFlowContent(
                        makeActivity(ActivityType.UPLOAD_SOURCE_DOCUMENT,
                                textFlowTarget)))
                .isEqualTo("");
    }


    private Activity makeActivity(ActivityType activityType,
            IsEntityWithType target) {
        return new Activity(new HPerson(), iteration, target,
                activityType, wordCount);
    }
}
