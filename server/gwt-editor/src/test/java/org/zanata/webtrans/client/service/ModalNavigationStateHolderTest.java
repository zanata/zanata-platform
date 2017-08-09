package org.zanata.webtrans.client.service;

import static org.zanata.webtrans.test.GWTTestData.makeTransUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.ui.UserConfigHolder;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.NavOption;
import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;

public class ModalNavigationStateHolderTest {
    private ModalNavigationStateHolder navigationStateHolder;
    // @formatter:off
    private final List<TransUnit> tuList = Lists.newArrayList(
            makeTransUnit(0, ContentState.New), makeTransUnit(1, ContentState.New), makeTransUnit(2, ContentState.NeedReview), makeTransUnit(3, ContentState.Approved), makeTransUnit(4, ContentState.NeedReview), makeTransUnit(5, ContentState.New), makeTransUnit(6, ContentState.NeedReview), makeTransUnit(7, ContentState.Approved), makeTransUnit(8, ContentState.New), makeTransUnit(9, ContentState.New), makeTransUnit(10, ContentState.NeedReview));
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
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(0);
    }

    @Test
    public void testGetNextRow() {
        navigationStateHolder.updateSelected(new TransUnitId(0));
        assertThat(navigationStateHolder.getNextId().getId()).isEqualTo(1L);
        navigationStateHolder.updateSelected(new TransUnitId(3));
        assertThat(navigationStateHolder.getNextId().getId()).isEqualTo(4L);
        navigationStateHolder.updateSelected(new TransUnitId(5));
        assertThat(navigationStateHolder.getNextId().getId()).isEqualTo(6L);
    }

    @Test
    public void testGetNextRowWithContentStateFilterOn() {
        configHolder.setFilterByFuzzy(true);
        configHolder.setFilterByUntranslated(true);
        navigationStateHolder.updateSelected(new TransUnitId(2));
        assertThat(navigationStateHolder.getNextId().getId()).isEqualTo(4L);
        navigationStateHolder.updateSelected(new TransUnitId(4));
        assertThat(navigationStateHolder.getNextId().getId()).isEqualTo(5L);
    }

    @Test
    public void testGetPrevRow() {
        navigationStateHolder.updateSelected(new TransUnitId(1));
        assertThat(navigationStateHolder.getPrevId().getId()).isEqualTo(0L);
        navigationStateHolder.updateSelected(new TransUnitId(4));
        assertThat(navigationStateHolder.getPrevId().getId()).isEqualTo(3L);
    }

    @Test
    public void testGetPrevRowWithContentStateFilterOn() {
        configHolder.setFilterByTranslated(true);
        configHolder.setFilterByUntranslated(true);
        navigationStateHolder.updateSelected(new TransUnitId(4));
        assertThat(navigationStateHolder.getPrevId().getId()).isEqualTo(3L);
        navigationStateHolder.updateSelected(new TransUnitId(3));
        assertThat(navigationStateHolder.getPrevId().getId()).isEqualTo(1L);
    }

    @Test
    public void testGetPreviousStateRow() {
        navigationStateHolder.updateSelected(new TransUnitId(9));
        configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);
        assertThat(navigationStateHolder.getPreviousStateId().getId()).isEqualTo(8L);
        navigationStateHolder.updateSelected(new TransUnitId(8));
        configHolder.setNavOption(NavOption.FUZZY);
        assertThat(navigationStateHolder.getPreviousStateId().getId()).isEqualTo(6);
        navigationStateHolder.updateSelected(new TransUnitId(4));
        configHolder.setNavOption(NavOption.UNTRANSLATED);
        assertThat(navigationStateHolder.getPreviousStateId().getId()).isEqualTo(1);
        navigationStateHolder.updateSelected(new TransUnitId(0));
        configHolder.setNavOption(NavOption.FUZZY);
        assertThat(navigationStateHolder.getPreviousStateId().getId()).isEqualTo(0);
    }

    @Test
    public void testGetNextStateRow() {
        navigationStateHolder.updateSelected(new TransUnitId(2));
        configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);
        assertThat(navigationStateHolder.getNextStateId().getId()).isEqualTo(4);
        navigationStateHolder.updateSelected(new TransUnitId(3));
        configHolder.setNavOption(NavOption.FUZZY);
        assertThat(navigationStateHolder.getNextStateId().getId()).isEqualTo(4);
        navigationStateHolder.updateSelected(new TransUnitId(7));
        configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);
        assertThat(navigationStateHolder.getNextStateId().getId()).isEqualTo(8);
        navigationStateHolder.updateSelected(new TransUnitId(5));
        configHolder.setNavOption(NavOption.UNTRANSLATED);
        assertThat(navigationStateHolder.getNextStateId().getId()).isEqualTo(8);
        navigationStateHolder.updateSelected(new TransUnitId(9));
        configHolder.setNavOption(NavOption.UNTRANSLATED);
        assertThat(navigationStateHolder.getNextStateId().getId()).isEqualTo(9);
    }

    @Test
    public void testUpdateMapAndNavigate() {
        navigationStateHolder.updateState(new TransUnitId(9L),
                ContentState.Approved);
        navigationStateHolder.updateSelected(new TransUnitId(10));
        assertThat(navigationStateHolder.getPreviousStateId().getId()).isEqualTo(8);
        navigationStateHolder.updateState(new TransUnitId(3L),
                ContentState.NeedReview);
        navigationStateHolder.updateSelected(new TransUnitId(2));
        assertThat(navigationStateHolder.getNextStateId().getId()).isEqualTo(3);
    }

    @Test
    public void canGetTargetPage() {
        // given page size is 3 and we have 11 trans unit
        // 0 1 2 | 3 4 5 | 6 7 8 | 9 10
        configHolder.setEditorPageSize(3);
        navigationStateHolder.init(transIdStateMap, idIndexList);
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(0)))
                .isEqualTo(0);
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(2)))
                .isEqualTo(0);
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(3)))
                .isEqualTo(1);
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(7)))
                .isEqualTo(2);
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(9)))
                .isEqualTo(3);
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(10)))
                .isEqualTo(3);
        assertThat(navigationStateHolder.getTargetPage(new TransUnitId(999)))
                .isEqualTo(NavigationService.UNDEFINED);
    }

    @Test
    public void canUpdatePageSize() {
        configHolder.setEditorPageSize(3);
        navigationStateHolder.init(transIdStateMap, idIndexList);
        assertThat(navigationStateHolder.getPageCount()).isEqualTo(4);
        navigationStateHolder.updateSelected(new TransUnitId(3));
        assertThat(navigationStateHolder.getNextStateId()).isEqualTo(new TransUnitId(4));
        configHolder.setEditorPageSize(4);
        navigationStateHolder.updatePageSize();
        assertThat(navigationStateHolder.getPageCount()).isEqualTo(3);
        assertThat(navigationStateHolder.getNextStateId()).isEqualTo(new TransUnitId(4));
    }
}
