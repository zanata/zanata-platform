/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.webtrans.client.service;

import java.util.List;
import java.util.Map;

import com.google.common.base.MoreObjects;
import org.zanata.common.ContentState;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/

@Singleton
public class ModalNavigationStateHolder {
    private final UserConfigHolder configHolder;
    private Map<TransUnitId, ContentState> idAndStateMap;
    private List<TransUnitId> idIndexList;

    private int curPage = 0;
    private TransUnitId selected = new TransUnitId(-1);

    // variables to save computation
    private transient int totalCount;
    private transient int pageCount;
    private transient int currentIndex;

    @Inject
    public ModalNavigationStateHolder(UserConfigHolder configHolder) {
        this.configHolder = configHolder;
    }

    protected void init(Map<TransUnitId, ContentState> transIdStateMap,
            List<TransUnitId> idIndexList) {
        this.idAndStateMap = transIdStateMap;
        this.idIndexList = idIndexList;
        totalCount = idIndexList.size();
        updatePageSize();
    }

    protected void updateState(TransUnitId id, ContentState newState) {
        idAndStateMap.put(id, newState);
    }

    private int maxRowIndex() {
        return totalCount - 1;
    }

    protected int getCurrentPage() {
        return curPage;
    }

    protected int getTargetPage(TransUnitId targetId) {
        int targetIndex = idIndexList.indexOf(targetId);
        if (targetIndex == NavigationService.UNDEFINED) {
            return NavigationService.UNDEFINED;
        }
        return targetIndex / configHolder.getState().getEditorPageSize();
    }

    protected int lastPage() {
        return pageCount == 0 ? 0 : pageCount - 1;
    }

    protected int getPageCount() {
        return pageCount;
    }

    protected void updateCurrentPage(int currentPageIndex) {
        curPage = currentPageIndex;
    }

    protected void updateSelected(TransUnitId id) {
        selected = id;
        currentIndex = idIndexList.indexOf(selected);
    }

    protected void updatePageSize() {
        pageCount =
                (int) Math.ceil(totalCount * 1.0
                        / configHolder.getState().getEditorPageSize());
    }

    protected TransUnitId getNextId() {
        if (configHolder.isAcceptAllStatus()) {
            return idIndexList.get(Math.min(currentIndex + 1, maxRowIndex()));
        }

        // we are in filter mode
        for (int i = currentIndex + 1; i <= maxRowIndex(); i++) {
            ContentState contentState = idAndStateMap.get(idIndexList.get(i));
            if (matchFilterCondition(contentState)) {
                return idIndexList.get(i);
            }
        }
        // nothing matches filter condition and has reached the end
        return selected;
    }

    private boolean matchFilterCondition(ContentState contentState) {
        return configHolder.getState().isFilterByFuzzy()
                && contentState.isRejectedOrFuzzy()
                || configHolder.getState().isFilterByUntranslated()
                && contentState == ContentState.New
                || configHolder.getState().isFilterByTranslated()
                && contentState.isTranslated();
    }

    public TransUnitId getPrevId() {
        if (configHolder.isAcceptAllStatus()) {
            return idIndexList.get(Math.max(currentIndex - 1, 0));
        }

        // we are in filter mode
        for (int i = currentIndex - 1; i >= 0; i--) {
            ContentState contentState = idAndStateMap.get(idIndexList.get(i));
            if (matchFilterCondition(contentState)) {
                return idIndexList.get(i);
            }
        }
        // nothing matches filter condition and has reached the end
        return selected;
    }

    public TransUnitId getPreviousStateId() {
        for (int i = currentIndex - 1; i >= 0; i--) {
            ContentState contentState = idAndStateMap.get(idIndexList.get(i));
            if (configHolder.getContentStatePredicate().apply(contentState)) {
                return idIndexList.get(i);
            }
        }
        return selected;
    }

    public TransUnitId getNextStateId() {
        for (int i = currentIndex + 1; i <= maxRowIndex(); i++) {
            ContentState contentState = idAndStateMap.get(idIndexList.get(i));
            if (configHolder.getContentStatePredicate().apply(contentState)) {
                return idIndexList.get(i);
            }
        }
        return selected;
    }

    public TransUnitId getFirstId() {
        return idIndexList.get(0);
    }

    public TransUnitId getLastId() {
        return idIndexList.get(maxRowIndex());
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this).add("idAndStateMap", idAndStateMap)
                .add("idIndexList", idIndexList).add("selected", selected)
                .add("curPage", curPage).add("totalCount", totalCount)
                .add("pageCount", pageCount).toString();
    }
}
