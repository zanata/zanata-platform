package org.zanata.webtrans.client.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.NavOption;
import com.google.common.collect.Lists;

public class ModalNavigationStateHolderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(ModalNavigationStateHolderTest.class);

    private ModalNavigationStateHolder navigationStateHolder;
    // @formatter:off
    private final List<TransUnit> tuList = Lists.newArrayList(TestFixture.makeTransUnit(0, ContentState.New), TestFixture.makeTransUnit(1, ContentState.New), TestFixture.makeTransUnit(2, ContentState.NeedReview), TestFixture.makeTransUnit(3, ContentState.Approved), TestFixture.makeTransUnit(4, ContentState.NeedReview), TestFixture.makeTransUnit(5, ContentState.New), TestFixture.makeTransUnit(6, ContentState.NeedReview), TestFixture.makeTransUnit(7, ContentState.Approved), TestFixture.makeTransUnit(8, ContentState.New), TestFixture.makeTransUnit(9, ContentState.New), TestFixture.makeTransUnit(10, ContentState.NeedReview));
    private Map<TransUnitId, ContentState> transIdStateMap;
    private List<TransUnitId> idIndexList;
    private UserConfigHolder configHolder;
    // @formatter:on
    // @Before
    // protected void setUpTestData() {
    // log.info("TransUnit list size: {}", tuList.size());
    // log.info("transIdStateMap : \n\t{}", transIdStateMap);
    // log.info("idIndexList : \n\t{}", idIndexList);
    // }

    @Before
    public void setUp() throws Exception {
        transIdStateMap = new HashMap<TransUnitId, ContentState>();
        idIndexList = new ArrayList<TransUnitId>();
        for (TransUnit tu : tuList) {
            transIdStateMap.put(tu.getId(), tu.getStatus());
            idIndexList.add(tu.getId());
        }
        configHolder = new UserConfigHolder();
        navigationStateHolder = new ModalNavigationStateHolder(configHolder);
        navigationStateHolder.init(transIdStateMap, idIndexList);
    }

    @Test
    public void testGetInitialPageSize() {
        assertThat(navigationStateHolder.getCurrentPage(), is(0));
    }

    @Test
    public void testGetNextRow() {
        navigationStateHolder.updateSelected(new TransUnitId(0));
        assertThat(navigationStateHolder.getNextId().getId(), is(1L));
        navigationStateHolder.updateSelected(new TransUnitId(3));
        assertThat(navigationStateHolder.getNextId().getId(), is(4L));
        navigationStateHolder.updateSelected(new TransUnitId(5));
        assertThat(navigationStateHolder.getNextId().getId(), is(6L));
    }

    @Test
    public void testGetNextRowWithContentStateFilterOn() {
        configHolder.setFilterByFuzzy(true);
        configHolder.setFilterByUntranslated(true);
        navigationStateHolder.updateSelected(new TransUnitId(2));
        assertThat(navigationStateHolder.getNextId().getId(), is(4L));
        navigationStateHolder.updateSelected(new TransUnitId(4));
        assertThat(navigationStateHolder.getNextId().getId(), is(5L));
    }

    @Test
    public void testGetPrevRow() {
        navigationStateHolder.updateSelected(new TransUnitId(1));
        assertThat(navigationStateHolder.getPrevId().getId(), is(0L));
        navigationStateHolder.updateSelected(new TransUnitId(4));
        assertThat(navigationStateHolder.getPrevId().getId(), is(3L));
    }

    @Test
    public void testGetPrevRowWithContentStateFilterOn() {
        configHolder.setFilterByTranslated(true);
        configHolder.setFilterByUntranslated(true);
        navigationStateHolder.updateSelected(new TransUnitId(4));
        assertThat(navigationStateHolder.getPrevId().getId(), is(3L));
        navigationStateHolder.updateSelected(new TransUnitId(3));
        assertThat(navigationStateHolder.getPrevId().getId(), is(1L));
    }

    @Test
    public void testGetPreviousStateRow() {
        navigationStateHolder.updateSelected(new TransUnitId(9));
        configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);
        assertThat(navigationStateHolder.getPreviousStateId().getId(), is(8L));
        navigationStateHolder.updateSelected(new TransUnitId(8));
        configHolder.setNavOption(NavOption.FUZZY);
        assertEquals(navigationStateHolder.getPreviousStateId().getId(), 6);
        navigationStateHolder.updateSelected(new TransUnitId(4));
        configHolder.setNavOption(NavOption.UNTRANSLATED);
        assertEquals(navigationStateHolder.getPreviousStateId().getId(), 1);
        navigationStateHolder.updateSelected(new TransUnitId(0));
        configHolder.setNavOption(NavOption.FUZZY);
        assertEquals(navigationStateHolder.getPreviousStateId().getId(), 0);
    }

    @Test
    public void testGetNextStateRow() {
        navigationStateHolder.updateSelected(new TransUnitId(2));
        configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);
        assertEquals(navigationStateHolder.getNextStateId().getId(), 4);
        navigationStateHolder.updateSelected(new TransUnitId(3));
        configHolder.setNavOption(NavOption.FUZZY);
        assertEquals(navigationStateHolder.getNextStateId().getId(), 4);
        navigationStateHolder.updateSelected(new TransUnitId(7));
        configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);
        assertEquals(navigationStateHolder.getNextStateId().getId(), 8);
        navigationStateHolder.updateSelected(new TransUnitId(5));
        configHolder.setNavOption(NavOption.UNTRANSLATED);
        assertEquals(navigationStateHolder.getNextStateId().getId(), 8);
        navigationStateHolder.updateSelected(new TransUnitId(9));
        configHolder.setNavOption(NavOption.UNTRANSLATED);
        assertEquals(navigationStateHolder.getNextStateId().getId(), 9);
    }

    @Test
    public void testUpdateMapAndNavigate() {
        navigationStateHolder.updateState(new TransUnitId(9L),
                ContentState.Approved);
        navigationStateHolder.updateSelected(new TransUnitId(10));
        assertEquals(navigationStateHolder.getPreviousStateId().getId(), 8);
        navigationStateHolder.updateState(new TransUnitId(3L),
                ContentState.NeedReview);
        navigationStateHolder.updateSelected(new TransUnitId(2));
        assertEquals(navigationStateHolder.getNextStateId().getId(), 3);
    }

    @Test
    public void canGetTargetPage() {
        // given page size is 3 and we have 11 trans unit
        // 0 1 2 | 3 4 5 | 6 7 8 | 9 10
        configHolder.setEditorPageSize(3);
        navigationStateHolder.init(transIdStateMap, idIndexList);
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(0)),
                Matchers.equalTo(0));
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(2)),
                Matchers.equalTo(0));
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(3)),
                Matchers.equalTo(1));
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(7)),
                Matchers.equalTo(2));
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(9)),
                Matchers.equalTo(3));
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(10)),
                Matchers.equalTo(3));
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(999)),
                Matchers.equalTo(NavigationService.UNDEFINED));
    }

    @Test
    public void canUpdatePageSize() {
        configHolder.setEditorPageSize(3);
        navigationStateHolder.init(transIdStateMap, idIndexList);
        assertThat(navigationStateHolder.getPageCount(), Matchers.equalTo(4));
        navigationStateHolder.updateSelected(new TransUnitId(3));
        assertThat(navigationStateHolder.getNextStateId(),
                equalTo(new TransUnitId(4)));
        configHolder.setEditorPageSize(4);
        navigationStateHolder.updatePageSize();
        assertThat(navigationStateHolder.getPageCount(), Matchers.equalTo(3));
        assertThat(navigationStateHolder.getNextStateId(),
                equalTo(new TransUnitId(4)));
    }
}
