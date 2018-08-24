package org.zanata.rest.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jglue.cdiunit.ContextController;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.ZanataTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.LocaleDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.po.HPoTargetHeader;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.test.CdiUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(CdiUnitRunner.class)
public class ResourceUtilsTest extends ZanataTest {
    private static final Logger log = LoggerFactory
            .getLogger(ResourceUtilsTest.class);

    @Inject
    private ResourceUtils resourceUtils;

    @Inject
    private ContextController contextController;

    @Produces @Mock
    private EntityManager mockEm;

    @Produces @Mock
    private LocaleDAO mockLocaleDAO;

    @BeforeClass
    public static void logMemoryForTests() {
        // FIXME why is this here?
        Runtime runtime = Runtime.getRuntime();
        log.info("total memory :" + runtime.totalMemory());
        log.info("unit tests free memory :" + runtime.freeMemory());
    }

    @Before
    public void initializeRequestScope() {
        // NB: this is easier than adding @InRequestScope to all test methods
        contextController.openRequest();
    }

    @Test
    public void getLanguage() {
        HLocale en = new HLocale(LocaleId.EN);
        assertThat(resourceUtils.getLanguage(en)).isEqualTo("en");
        HLocale enUS = new HLocale(LocaleId.EN_US);
        assertThat(resourceUtils.getLanguage(enUS)).isEqualTo("en_US");
    }

    @Test
    public void mergeNoTextFlows() {
        List<TextFlow> from = new ArrayList<>();
        HDocument to = new HDocument();
        boolean changed =
                resourceUtils.transferFromTextFlows(from, to,
                        new HashSet<>(), 1);

        assertThat(changed).isFalse();
    }

    @Test
    public void mergeTextFlowWithOneFromChange() {
        List<TextFlow> from = new ArrayList<TextFlow>();

        TextFlow tf1 = new TextFlow("id", LocaleId.EN, "text1");
        from.add(tf1);

        HDocument to = new HDocument();
        to.setLocale(new HLocale(LocaleId.EN_US));
        boolean changed =
                resourceUtils.transferFromTextFlows(from, to,
                        new HashSet<String>(), 1);

        assertThat(changed).isTrue();
    }

    @Test
    public void mergeChangedTextFlow() {
        // set up HDocument with a text flow
        HDocument to = new HDocument();
        to.setLocale(new HLocale(LocaleId.EN_US));
        int originalTFRevision = 1;
        to.setRevision(originalTFRevision);
        HTextFlow originalTF = new HTextFlow(to, "id", "original text");
        originalTF.setRevision(originalTFRevision);

        // target locales that will have new, fuzzy and approved targets
        HLocale newLoc, fuzzyLoc, apprLoc;
        newLoc = new HLocale(LocaleId.DE);
        fuzzyLoc = new HLocale(LocaleId.FR);
        apprLoc = new HLocale(LocaleId.ES);
        // Target Locale ids
        Long newLocId = 1L, fuzzyLocId = 2L, apprLocId = 3L;

        HTextFlowTarget newTarg, fuzzyTarg, apprTarg;
        newTarg = new HTextFlowTarget(originalTF, newLoc);
        fuzzyTarg = new HTextFlowTarget(originalTF, fuzzyLoc);
        apprTarg = new HTextFlowTarget(originalTF, apprLoc);

        int newTargVersionBefore = 1;
        int fuzzyTargVersionBefore = 1;
        int apprTargVersionBefore = 1;

        newTarg.setVersionNum(newTargVersionBefore);
        fuzzyTarg.setVersionNum(fuzzyTargVersionBefore);
        apprTarg.setVersionNum(apprTargVersionBefore);

        newTarg.setState(ContentState.New);
        fuzzyTarg.setState(ContentState.NeedReview);
        apprTarg.setState(ContentState.Approved);

        originalTF.getTargets().put(newLocId, newTarg);
        originalTF.getTargets().put(fuzzyLocId, fuzzyTarg);
        originalTF.getTargets().put(apprLocId, apprTarg);

        to.getAllTextFlows().put("id", originalTF);

        // set up a textflow with the same id and different content
        TextFlow changedTF =
                new TextFlow(originalTF.getResId(), LocaleId.EN,
                        "changed text");
        List<TextFlow> from = new ArrayList<TextFlow>();
        from.add(changedTF);

        int newTFRevision = 2;

        boolean changed =
                resourceUtils.transferFromTextFlows(from, to,
                        new HashSet<String>(), newTFRevision);

        Map<Long, HTextFlowTarget> targets =
                to.getAllTextFlows().get("id").getTargets();
        newTarg = targets.get(newLocId);
        assertThat(newTarg.getState()).isEqualTo(ContentState.New);
        assertThat(newTarg.getVersionNum()).isEqualTo(newTargVersionBefore);
        assertThat(newTarg.getTextFlowRevision()).isEqualTo(originalTFRevision);

        fuzzyTarg = targets.get(fuzzyLocId);
        assertThat(fuzzyTarg.getState()).isEqualTo(ContentState.NeedReview);
        assertThat(fuzzyTarg.getVersionNum()).isEqualTo(fuzzyTargVersionBefore);
        assertThat(fuzzyTarg.getTextFlowRevision())
                .isEqualTo(originalTFRevision);

        apprTarg = targets.get(apprLocId);
        assertThat(apprTarg.getState())
                .as("approved targets should be set to fuzzy when source content changes")
                .isEqualTo(ContentState.NeedReview);
        assertThat(apprTarg.getVersionNum())
                .isEqualTo(apprTargVersionBefore + 1);
        // Note: TFTRevision should be updated when target content or state is
        // changed in editor, not here.
        assertThat(apprTarg.getTextFlowRevision())
                .isEqualTo(originalTFRevision);

        assertThat(changed).isTrue();
    }

    @Test
    public void pushCommentInitialImport() {
        PoTargetHeader fromHeader = new PoTargetHeader();
        String comment = "comment to import\nsecond line";
        fromHeader.setComment(comment);
        HPoTargetHeader toHeader = new HPoTargetHeader();
        resourceUtils.pushPoTargetComment(fromHeader, toHeader, MergeType.AUTO);
        assertThat(toHeader.getComment().getComment()).isEqualTo(comment);
    }

    @Test
    public void pushCommentSkip() {
        PoTargetHeader fromHeader = new PoTargetHeader();
        String comment = "comment to import\nskip this #zanata\nlast line";
        String expectedComment = "comment to import\nlast line";
        fromHeader.setComment(comment);
        HPoTargetHeader toHeader = new HPoTargetHeader();
        resourceUtils.pushPoTargetComment(fromHeader, toHeader,
                MergeType.IMPORT);
        assertThat(toHeader.getComment().getComment()).isEqualTo(expectedComment);
    }

    @Test
    public void pushCommentMerge() {
        PoTargetHeader fromHeader = new PoTargetHeader();
        String importedComment = "initial comment\nAlice #zanata\nCharlie";
        String expectedComment = "initial comment\nBob\nCharlie";
        fromHeader.setComment(importedComment);
        HPoTargetHeader toHeader = new HPoTargetHeader();
        toHeader.setComment(new HSimpleComment("initial comment\nBob"));
        resourceUtils.pushPoTargetComment(fromHeader, toHeader, MergeType.AUTO);
        assertThat(toHeader.getComment().getComment()).isEqualTo(expectedComment);
    }

    @Test
    public void pushCommentImport() {
        PoTargetHeader fromHeader = new PoTargetHeader();
        String importedComment = "initial comment\nAlice #zanata\nCharlie";
        String expectedComment = "initial comment\nCharlie";
        fromHeader.setComment(importedComment);
        HPoTargetHeader toHeader = new HPoTargetHeader();
        toHeader.setComment(new HSimpleComment("initial comment\nBob"));
        resourceUtils.pushPoTargetComment(fromHeader, toHeader,
                MergeType.IMPORT);
        assertThat(toHeader.getComment().getComment()).isEqualTo(expectedComment);
    }

    @Test
    public void pullCommentEmpty() {

        HPoTargetHeader fromHeader = new HPoTargetHeader();
        PoTargetHeader toHeader = new PoTargetHeader();

        List<HTextFlowTarget> hTargets = Collections.emptyList();
        resourceUtils.pullPoTargetComment(fromHeader, toHeader, hTargets);

        assertThat(toHeader.getComment()).isEqualTo("");
    }

    @Test
    public void pullCommentInitial() {

        HPoTargetHeader fromHeader = new HPoTargetHeader();
        fromHeader.setComment(new HSimpleComment("initial comment"));
        String expectedComment = "initial comment";
        PoTargetHeader toHeader = new PoTargetHeader();

        List<HTextFlowTarget> hTargets = Collections.emptyList();
        resourceUtils.pullPoTargetComment(fromHeader, toHeader, hTargets);

        assertThat(toHeader.getComment()).isEqualTo(expectedComment);
    }

    @Test
    public void pullCommentWithCredits() {

        HPoTargetHeader fromHeader = new HPoTargetHeader();
        fromHeader.setComment(new HSimpleComment("initial comment"));
        String expectedComment =
                "initial comment\n"
                        + "Alice <alice@example.org>, 2010. #zanata\n"
                        + "Alice <alice@example.org>, 2011. #zanata";
        PoTargetHeader toHeader = new PoTargetHeader();

        HPerson alice = new HPerson();
        alice.setName("Alice");
        alice.setEmail("alice@example.org");
        List<HTextFlowTarget> hTargets = new ArrayList<HTextFlowTarget>();
        HTextFlowTarget tft1 = new HTextFlowTarget();
        tft1.setLastChanged(new Date(1302671654000L)); // 13 Apr 2011
        tft1.setLastModifiedBy(alice);
        hTargets.add(tft1);

        HTextFlowTarget tft2 = new HTextFlowTarget();
        tft2.setLastChanged(new Date(1304329523000L)); // 2 May 2011
        tft2.setLastModifiedBy(alice);
        hTargets.add(tft2);

        HTextFlowTarget tft3 = new HTextFlowTarget();
        tft3.setLastChanged(new Date(1262419384000L)); // 2 Jan 2010
        tft3.setLastModifiedBy(alice);
        hTargets.add(tft3);

        resourceUtils.pullPoTargetComment(fromHeader, toHeader, hTargets);

        assertThat(toHeader.getComment()).isEqualTo(expectedComment);
    }

    @Test
    public void splitLinesSimple() {
        String s = "1\n2\n3";
        List<String> expected = Arrays.asList("1", "2", "3");
        List<String> lines = ResourceUtils.splitLines(s, null);
        assertThat(lines).isEqualTo(expected);
    }

    @Test
    public void splitLinesEmpty() {
        String s = "";
        List<String> expected = Collections.emptyList();
        List<String> lines = ResourceUtils.splitLines(s, null);
        assertThat(lines).isEqualTo(expected);
    }

    @Test
    public void splitLinesSkip() {
        String s = "1\n2 #zanata\n3";
        List<String> expected = Arrays.asList("1", "3");
        List<String> lines = ResourceUtils.splitLines(s, "#zanata");
        assertThat(lines).isEqualTo(expected);
    }

    @Test
    public void splitLinesSkipAll() {
        String s = "1 #zanata\n2 #zanata\n3 #zanata";
        List<String> expected = Collections.emptyList();
        List<String> lines = ResourceUtils.splitLines(s, "#zanata");
        assertThat(lines).isEqualTo(expected);
    }

    /**
     * Tests that all plural information is readable
     */
    @Test
    public void readPluralForms() {
        Properties properties = resourceUtils.getPluralForms();

        for (Object key : properties.keySet()) {
            String propKey = (String) key;
            LocaleId localeId = LocaleId.fromJavaName(propKey);
            resourceUtils.getPluralForms(localeId, true, false);
            verify(mockLocaleDAO).findByLocaleId(localeId);
            resourceUtils.getNumPlurals(null, localeId);
        }
    }

    @Test
    public void pluralFormsAlternateSeparators() {
        // Plural forms for "es"
        String esPluralForm = resourceUtils.getPluralForms(LocaleId.ES);

        verify(mockLocaleDAO).findByLocaleId(LocaleId.ES);

        assertThat(esPluralForm).isNotNull();

        // Alternate forms that should match the "es" plurals
        // "es_ES"
        assertThat(resourceUtils.getPluralForms(new LocaleId("es-ES")))
                .isEqualTo(esPluralForm);
        // "es.ES"
        assertThat(resourceUtils.getPluralForms(new LocaleId("es.ES")))
                .isEqualTo(esPluralForm);
        // "es@ES"
        assertThat(resourceUtils.getPluralForms(new LocaleId("es@ES")))
                .isEqualTo(esPluralForm);
        // "es.ES@Latin"
        assertThat(resourceUtils.getPluralForms(new LocaleId("es.ES@Latin")))
                .isEqualTo(esPluralForm);
    }

    @Test
    public void pluralFormsTest() {

        //given mock data
        String testPluralForms = "testPluralForms";
        HLocale mockHLocale = new HLocale(LocaleId.ES);
        mockHLocale.setPluralForms(testPluralForms);
        when(mockLocaleDAO.findByLocaleId(LocaleId.ES)).thenReturn(mockHLocale);

        //execute
        String pluralForms = resourceUtils.getPluralForms(LocaleId.ES);

        //verify and assert
        verify(mockLocaleDAO).findByLocaleId(LocaleId.ES);
        assertThat(pluralForms).isEqualTo(testPluralForms);
    }

    @Test
    public void pluralFormsTestUseDBEntryTest() {

        //given mock data
        HLocale mockHLocale = new HLocale(LocaleId.ES);
        when(mockLocaleDAO.findByLocaleId(LocaleId.ES)).thenReturn(mockHLocale);

        //execute
        String pluralForms = resourceUtils.getPluralForms(LocaleId.ES);

        //verify and assert
        verify(mockLocaleDAO).findByLocaleId(LocaleId.ES);
        assertThat(pluralForms).isNotNull();
    }

    @Test
    public void isValidPluralFormsTest() {
        String invalidPluralForms = "testPluralForms";
        assertThat(resourceUtils.isValidPluralForms(invalidPluralForms)).isFalse();

        invalidPluralForms = "nplurals=notinteger";
        assertThat(resourceUtils.isValidPluralForms(invalidPluralForms)).isFalse();

        invalidPluralForms = "nplurals=-1";
        assertThat(resourceUtils.isValidPluralForms(invalidPluralForms)).isFalse();

        invalidPluralForms = "nplurals=" + ResourceUtils.MAX_TARGET_CONTENTS + 1;
        assertThat(resourceUtils.isValidPluralForms(invalidPluralForms)).isFalse();

        invalidPluralForms = "nplurals=0";
        assertThat(resourceUtils.isValidPluralForms(invalidPluralForms)).isFalse();

        invalidPluralForms = "nplurals=1;plural=0";
        assertThat(resourceUtils.isValidPluralForms(invalidPluralForms)).isTrue();

        invalidPluralForms = "nplurals=5; plural=10" +
                ResourceUtils.MAX_TARGET_CONTENTS;
        assertThat(resourceUtils.isValidPluralForms(invalidPluralForms)).isTrue();
    }
}
