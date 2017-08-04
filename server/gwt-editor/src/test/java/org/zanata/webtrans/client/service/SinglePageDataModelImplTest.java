package org.zanata.webtrans.client.service;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.webtrans.client.service.NavigationService.UNDEFINED;
import static org.zanata.webtrans.test.GWTTestData.makeTransUnit;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SinglePageDataModelImplTest {
    private SinglePageDataModelImpl model;
    // @formatter:off
    private List<TransUnit> data = Lists.newArrayList(
        makeTransUnit(1),
        makeTransUnit(2),
        makeTransUnit(3)
    );
    // @formatter:on

    @Before
    public void setUp() throws Exception {
        model = new SinglePageDataModelImpl();
    }

    @Test
    public void canSetDataAndResetCurrentRowToUnselected() {
        assertThat(model.getData()).isEmpty();

        model.setData(data);

        assertThat(model.getCurrentRow()).isEqualTo(UNDEFINED);
        assertThat(model.getData()).isEqualTo(data);

        model.setSelected(1);
        assertThat(model.getCurrentRow()).isEqualTo(1);
        model.setData(data);
        assertThat(model.getCurrentRow()).isEqualTo(UNDEFINED);
    }

    @Test
    public void canGetByIdOrNull() {
        model.setData(data);

        TransUnit found = model.getByIdOrNull(new TransUnitId(2));
        assertThat(found).isEqualTo(data.get(1));

        TransUnit notFound = model.getByIdOrNull(new TransUnitId(99));
        assertThat(notFound).isNull();
    }

    @Test
    public void canFindIndexById() {
        model.setData(data);

        int index = model.findIndexById(new TransUnitId(3));
        assertThat(index).isEqualTo(2);

        int notFoundIndex = model.findIndexById(new TransUnitId(99));
        assertThat(notFoundIndex).isEqualTo(UNDEFINED);
    }

    @Test
    public void canSetSelectedByIndex() {
        model.setData(data);

        model.setSelected(2);

        assertThat(model.getCurrentRow()).isEqualTo(2);
        assertThat(model.getSelectedOrNull()).isSameAs(data.get(2));
    }

    @Test
    public void selectedIsNullIfIndexIsOutOfRange() {
        model.setData(data);

        model.setSelected(-1);

        TransUnit result = model.getSelectedOrNull();
        assertThat(result).isNull();
    }

    @Test
    public void willUpdateModelIfInCurrentPage() {
        TransUnitId updatedTUId = new TransUnitId(3);
        TransUnit updatedTransUnit =
                makeTransUnit(3, ContentState.Approved);
        List<TransUnit> oldData = ImmutableList.copyOf(data);

        model.setData(data);
        assertThat(model.getByIdOrNull(updatedTUId))
                .isNotSameAs(updatedTransUnit);

        boolean updated = model.updateIfInCurrentPage(updatedTransUnit);

        assertThat(updated).isTrue();
        assertThat(model.getData()).isNotEqualTo(oldData);
        assertThat(model.getByIdOrNull(updatedTUId)).isSameAs(updatedTransUnit);
    }

    @Test
    public void willNotUpdateModelIfNotInCurrentPage() {
        TransUnitId updatedTUId = new TransUnitId(9);
        TransUnit updatedTransUnit =
                makeTransUnit(9, ContentState.Approved);
        List<TransUnit> oldData = ImmutableList.copyOf(data);

        model.setData(data);
        assertThat(model.getByIdOrNull(updatedTUId)).isNotSameAs(updatedTransUnit);

        boolean updated = model.updateIfInCurrentPage(updatedTransUnit);

        assertThat(updated).isFalse();
        assertThat(model.getData()).isEqualTo(oldData);
    }
}
