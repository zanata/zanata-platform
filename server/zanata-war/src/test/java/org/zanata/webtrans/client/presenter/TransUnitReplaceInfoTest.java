package org.zanata.webtrans.client.presenter;

import java.util.Comparator;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdatePreview;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
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

        assertThat(comparator).isSameAs(sameComparator);
    }

    @Test
    public void rowComparatorComparesOnRowIndex() {
        Comparator<TransUnitReplaceInfo> comparator =
                TransUnitReplaceInfo.getRowComparator();

        assertThat(comparator.compare(null, null)).isEqualTo(0);
        assertThat(comparator.compare(null, newReplaceInfo(1)))
                .isEqualTo(-1);
        assertThat(comparator.compare(newReplaceInfo(1), null))
                .isEqualTo(1);
        assertThat(comparator.compare(newReplaceInfo(1), newReplaceInfo(1)))
                .isEqualTo(0);
        assertThat(comparator.compare(newReplaceInfo(1), newReplaceInfo(2)))
                .isEqualTo(-1);
        assertThat(comparator.compare(newReplaceInfo(2), newReplaceInfo(1)))
                .isEqualTo(1);
    }

    @Test
    public void testSetterAndGetter() {
        MockitoAnnotations.initMocks(this);
        TransUnit transUnit = TestFixture.makeTransUnit(1);
        TransUnitReplaceInfo info =
                new TransUnitReplaceInfo(CONTAINING_DOC_ID, transUnit);

        // init state
        assertThat(info.getDocId()).isEqualTo(CONTAINING_DOC_ID);
        assertThat(info.getPreview()).isNull();
        assertThat(info.getPreviewState()).isEqualTo(PreviewState.NotFetched);
        assertThat(info.getReplaceInfo()).isNull();
        assertThat(info.getReplaceState()).isEqualTo(ReplacementState.NotReplaced);
        assertThat(info.getTransUnit()).isSameAs(transUnit);

        // update state
        info.setPreview(updatePreview);
        info.setPreviewState(PreviewState.Show);
        info.setReplaceInfo(updateInfo);
        info.setReplaceState(ReplacementState.Replaced);
        TransUnit newTU = TestFixture.makeTransUnit(2);
        info.setTransUnit(newTU);

        assertThat(info.getPreview()).isSameAs(updatePreview);
        assertThat(info.getPreviewState()).isEqualTo(PreviewState.Show);
        assertThat(info.getReplaceInfo()).isSameAs(updateInfo);
        assertThat(info.getReplaceState()).isSameAs(ReplacementState.Replaced);
        assertThat(info.getTransUnit()).isSameAs(newTU);
    }
}
