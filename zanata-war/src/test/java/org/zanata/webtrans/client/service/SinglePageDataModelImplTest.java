package org.zanata.webtrans.client.service;

import java.util.List;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.*;
import static org.zanata.webtrans.client.service.NavigationService.UNDEFINED;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class SinglePageDataModelImplTest {
    private SinglePageDataModelImpl model;
    // @formatter:off
    private List<TransUnit> data = Lists.newArrayList(
        TestFixture.makeTransUnit(1),
        TestFixture.makeTransUnit(2),
        TestFixture.makeTransUnit(3)
    );
    // @formatter:on

    @BeforeMethod
    public void setUp() throws Exception {
        model = new SinglePageDataModelImpl();
    }

    @Test
    public void canSetDataAndResetCurrentRowToUnselected() {
        assertThat(model.getData(), Matchers.is(Matchers.<TransUnit> empty()));

        model.setData(data);

        assertThat(model.getCurrentRow(), Matchers.equalTo(UNDEFINED));
        assertThat(model.getData(), Matchers.is(data));

        model.setSelected(1);
        assertThat(model.getCurrentRow(), Matchers.equalTo(1));
        model.setData(data);
        assertThat(model.getCurrentRow(), Matchers.equalTo(UNDEFINED));
    }

    @Test
    public void canGetByIdOrNull() {
        model.setData(data);

        TransUnit found = model.getByIdOrNull(new TransUnitId(2));
        assertThat(found, Matchers.equalTo(data.get(1)));

        TransUnit notFound = model.getByIdOrNull(new TransUnitId(99));
        assertThat(notFound, Matchers.is(Matchers.nullValue()));
    }

    @Test
    public void canFindIndexById() {
        model.setData(data);

        int index = model.findIndexById(new TransUnitId(3));
        assertThat(index, Matchers.is(2));

        int notFoundIndex = model.findIndexById(new TransUnitId(99));
        assertThat(notFoundIndex, Matchers.is(UNDEFINED));
    }

    @Test
    public void canSetSelectedByIndex() {
        model.setData(data);

        model.setSelected(2);

        assertThat(model.getCurrentRow(), Matchers.is(2));
        assertThat(model.getSelectedOrNull(),
                Matchers.sameInstance(data.get(2)));
    }

    @Test
    public void selectedIsNullIfIndexIsOutOfRange() {
        model.setData(data);

        model.setSelected(-1);

        TransUnit result = model.getSelectedOrNull();
        assertThat(result, Matchers.is(Matchers.nullValue()));
    }

    @Test
    public void willUpdateModelIfInCurrentPage() {
        TransUnitId updatedTUId = new TransUnitId(3);
        TransUnit updatedTransUnit =
                TestFixture.makeTransUnit(3, ContentState.Approved);
        List<TransUnit> oldData = ImmutableList.copyOf(data);

        model.setData(data);
        assertThat(model.getByIdOrNull(updatedTUId),
                Matchers.not(Matchers.sameInstance(updatedTransUnit)));

        boolean updated = model.updateIfInCurrentPage(updatedTransUnit);

        assertThat(updated, Matchers.is(true));
        assertThat(model.getData(), Matchers.not(Matchers.equalTo(oldData)));
        assertThat(model.getByIdOrNull(updatedTUId),
                Matchers.sameInstance(updatedTransUnit));
    }

    @Test
    public void willNotUpdateModelIfNotInCurrentPage() {
        TransUnitId updatedTUId = new TransUnitId(9);
        TransUnit updatedTransUnit =
                TestFixture.makeTransUnit(9, ContentState.Approved);
        List<TransUnit> oldData = ImmutableList.copyOf(data);

        model.setData(data);
        assertThat(model.getByIdOrNull(updatedTUId),
                Matchers.not(Matchers.sameInstance(updatedTransUnit)));

        boolean updated = model.updateIfInCurrentPage(updatedTransUnit);

        assertThat(updated, Matchers.is(false));
        assertThat(model.getData(), Matchers.equalTo(oldData));
    }
}
