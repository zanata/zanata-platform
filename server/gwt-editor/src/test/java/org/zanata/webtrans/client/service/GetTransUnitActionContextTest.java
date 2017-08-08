package org.zanata.webtrans.client.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.webtrans.test.GWTTestData.documentInfo;

import org.junit.Before;
import org.junit.Test;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.GetTransUnitActionContext;
import org.zanata.webtrans.shared.model.TransUnitId;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GetTransUnitActionContextTest {
    private static final boolean NEEDED = true;
    private static final boolean NO_NEED = false;
    private GetTransUnitActionContext context;

    @Before
    public void setUp() throws Exception {
        context = new GetTransUnitActionContext(documentInfo(1, ""));
    }

    @Test
    public void testChangeDocument() throws Exception {
        DocumentInfo document = context.getDocument();
        GetTransUnitActionContext newContext =
                context.changeDocument(documentInfo(99, ""));

        assertThat(context.getDocument()).isSameAs(document);
        assertThat(newContext.getDocument()).isEqualTo(documentInfo(99, ""));
    }

    @Test
    public void testChangeFindMessage() throws Exception {
        String findMessage = context.getFindMessage();
        GetTransUnitActionContext result =
                context.withFindMessage("new find message");

        assertThat(context.getFindMessage()).isSameAs(findMessage);
        assertThat(result.getFindMessage()).isEqualTo("new find message");
    }

    @Test
    public void testChangeFilterTranslated() throws Exception {
        GetTransUnitActionContext result = context.withFilterTranslated(true);

        assertThat(context.isFilterTranslated()).isFalse();
        assertThat(result.isFilterTranslated()).isTrue();
    }

    @Test
    public void testChangeFilterNeedReview() throws Exception {
        GetTransUnitActionContext result = context.withFilterFuzzy(true);

        assertThat(context.isFilterFuzzy()).isFalse();
        assertThat(result.isFilterFuzzy()).isTrue();
    }

    @Test
    public void testChangeFilterUntranslated() throws Exception {
        GetTransUnitActionContext result =
                context.withFilterUntranslated(true);

        assertThat(context.isFilterUntranslated()).isFalse();
        assertThat(result.isFilterUntranslated()).isTrue();
    }

    @Test
    public void testChangeFilterApproved() throws Exception {
        GetTransUnitActionContext result = context.withFilterApproved(true);

        assertThat(context.isFilterApproved()).isFalse();
        assertThat(result.isFilterApproved()).isTrue();
    }

    @Test
    public void testChangeFilterRejected() throws Exception {
        GetTransUnitActionContext result = context.withFilterRejected(true);

        assertThat(context.isFilterRejected()).isFalse();
        assertThat(result.isFilterRejected()).isTrue();
    }

    @Test
    public void testNeedReloadList() throws Exception {
        verifyNeedReloadTransUnits(context, context.withFilterFuzzy(true),
                NEEDED);
        verifyNeedReloadTransUnits(context,
                context.withFilterUntranslated(true), NEEDED);
        verifyNeedReloadTransUnits(context,
                context.withFilterTranslated(true), NEEDED);
        verifyNeedReloadTransUnits(context, context.withFilterApproved(true),
                NEEDED);
        verifyNeedReloadTransUnits(context, context.withFilterRejected(true),
                NEEDED);
        verifyNeedReloadTransUnits(context,
                context.changeDocument(documentInfo(99, "")), NEEDED);
        verifyNeedReloadTransUnits(context,
                context.withFindMessage("find message"), NEEDED);
        verifyNeedReloadTransUnits(context, context.withCount(2), NEEDED);
        verifyNeedReloadTransUnits(context, context.withOffset(2), NEEDED);
        verifyNeedReloadTransUnits(context,
                context.withTargetTransUnitId(new TransUnitId(2)), NO_NEED);
    }

    private static void verifyNeedReloadTransUnits(
            GetTransUnitActionContext context,
            GetTransUnitActionContext newContext, boolean needed) {
        boolean result = context.needReloadList(newContext);
        assertThat(result).isEqualTo(needed);
    }

    @Test
    public void testNeedReloadNavigationIndex() throws Exception {
        verifyNeedReloadNavigationIndex(context,
                context.withFilterFuzzy(true), NEEDED);
        verifyNeedReloadNavigationIndex(context,
                context.withFilterUntranslated(true), NEEDED);
        verifyNeedReloadNavigationIndex(context,
                context.withFilterTranslated(true), NEEDED);
        verifyNeedReloadNavigationIndex(context,
                context.withFilterApproved(true), NEEDED);
        verifyNeedReloadNavigationIndex(context,
                context.withFilterRejected(true), NEEDED);
        verifyNeedReloadNavigationIndex(context,
                context.changeDocument(documentInfo(99, "")), NEEDED);
        verifyNeedReloadNavigationIndex(context,
                context.withFindMessage("find message"), NEEDED);

        verifyNeedReloadNavigationIndex(context, context.withCount(2),
                NO_NEED);
        verifyNeedReloadNavigationIndex(context, context.withOffset(2),
                NEEDED);
        verifyNeedReloadNavigationIndex(context,
                context.withTargetTransUnitId(new TransUnitId(2)), NO_NEED);
    }

    private static void verifyNeedReloadNavigationIndex(
            GetTransUnitActionContext context,
            GetTransUnitActionContext newContext, boolean needed) {
        boolean result = context.needReloadNavigationIndex(newContext);
        assertThat(result).isEqualTo(needed);
    }
}
