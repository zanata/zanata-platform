package org.zanata.webtrans.client.presenter;

import java.util.Comparator;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.Test;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdatePreview;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransUnitReplaceInfoTest {
    public static final long CONTAINING_DOC_ID = 1L;
    @Mock
    private TransUnitUpdatePreview updatePreview;
    @Mock
    private TransUnitUpdateInfo updateInfo;

    private static TransUnitReplaceInfo newReplaceInfo(int idAndRowIndex) {
        return new TransUnitReplaceInfo(CONTAINING_DOC_ID,
                TestFixture.makeTransUnit(idAndRowIndex));
    }

    @Test
    public void getRowComparatorReturnSingleton() throws Exception {
        Comparator<TransUnitReplaceInfo> comparator =
                TransUnitReplaceInfo.getRowComparator();
        Comparator<TransUnitReplaceInfo> sameComparator =
                TransUnitReplaceInfo.getRowComparator();

        assertThat(comparator, Matchers.sameInstance(sameComparator));
    }

    @Test
    public void rowComparatorComparesOnRowIndex() {
        Comparator<TransUnitReplaceInfo> comparator =
                TransUnitReplaceInfo.getRowComparator();

        assertThat(comparator.compare(null, null), Matchers.equalTo(0));
        assertThat(comparator.compare(null, newReplaceInfo(1)),
                Matchers.equalTo(-1));
        assertThat(comparator.compare(newReplaceInfo(1), null),
                Matchers.equalTo(1));
        assertThat(comparator.compare(newReplaceInfo(1), newReplaceInfo(1)),
                Matchers.equalTo(0));
        assertThat(comparator.compare(newReplaceInfo(1), newReplaceInfo(2)),
                Matchers.equalTo(-1));
        assertThat(comparator.compare(newReplaceInfo(2), newReplaceInfo(1)),
                Matchers.equalTo(1));
    }

    @Test
    public void testSetterAndGetter() {
        MockitoAnnotations.initMocks(this);
        TransUnit transUnit = TestFixture.makeTransUnit(1);
        TransUnitReplaceInfo info =
                new TransUnitReplaceInfo(CONTAINING_DOC_ID, transUnit);

        // init state
        assertThat(info.getDocId(), Matchers.equalTo(CONTAINING_DOC_ID));
        assertThat(info.getPreview(), Matchers.nullValue());
        assertThat(info.getPreviewState(),
                Matchers.equalTo(PreviewState.NotFetched));
        assertThat(info.getReplaceInfo(), Matchers.nullValue());
        assertThat(info.getReplaceState(),
                Matchers.equalTo(ReplacementState.NotReplaced));
        assertThat(info.getTransUnit(), Matchers.sameInstance(transUnit));

        // update state
        info.setPreview(updatePreview);
        info.setPreviewState(PreviewState.Show);
        info.setReplaceInfo(updateInfo);
        info.setReplaceState(ReplacementState.Replaced);
        TransUnit newTU = TestFixture.makeTransUnit(2);
        info.setTransUnit(newTU);

        assertThat(info.getPreview(), Matchers.sameInstance(updatePreview));
        assertThat(info.getPreviewState(), Matchers.equalTo(PreviewState.Show));
        assertThat(info.getReplaceInfo(), Matchers.sameInstance(updateInfo));
        assertThat(info.getReplaceState(),
                Matchers.equalTo(ReplacementState.Replaced));
        assertThat(info.getTransUnit(), Matchers.sameInstance(newTU));
    }
}
