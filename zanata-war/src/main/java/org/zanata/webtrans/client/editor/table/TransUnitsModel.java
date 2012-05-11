/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.webtrans.client.editor.table;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.table.EditRowCallback;
import org.zanata.webtrans.shared.model.TransUnit;

import java.util.List;
import java.util.ListIterator;

@Singleton
public class TransUnitsModel implements EditRowCallback {
    public static final int INIT_INDEX = 0;
    private static final Predicate<TransUnit> FUZZY_OR_NEW_PREDICATE = new Predicate<TransUnit>() {
        @Override
        public boolean apply(TransUnit transUnit) {
            return transUnit.getStatus() == ContentState.New || transUnit.getStatus() == ContentState.NeedReview;
        }
    };
    private static final Predicate<TransUnit> FUZZY_PREDICATE = new Predicate<TransUnit>() {
        @Override
        public boolean apply(TransUnit transUnit) {
            return transUnit.getStatus() == ContentState.NeedReview;
        }
    };
    private static final Predicate<TransUnit> NEW_PREDICATE = new Predicate<TransUnit>() {
        @Override
        public boolean apply(TransUnit transUnit) {
            return transUnit.getStatus() == ContentState.New;
        }
    };

    private int currentIndex = INIT_INDEX;
    private List<TransUnit> transUnits = Lists.newArrayList();

    public void setTransUnits(List<TransUnit> newTransUnits) {
        this.transUnits = ImmutableList.copyOf(newTransUnits);
        resetAllIndexes();
    }

    private void resetAllIndexes() {
        currentIndex = INIT_INDEX;
    }

    @Override
    public void gotoNextRow() {
        if (currentIndex < transUnits.size() - 1) {
            currentIndex++;
        }
    }

    @Override
    public void gotoPrevRow() {
        if (currentIndex > 0) {
            currentIndex--;
        }
    }

    @Override
    public void gotoFirstRow() {
        currentIndex = 0;
    }

    @Override
    public void gotoLastRow() {
        currentIndex = transUnits.size() - 1;
    }

    @Override
    public void gotoNextFuzzyNewRow() {
        moveForwardAndFind(FUZZY_OR_NEW_PREDICATE);
    }

    @Override
    public void gotoPrevFuzzyNewRow() {
        moveBackwardAndFind(FUZZY_OR_NEW_PREDICATE);
    }

    @Override
    public void gotoNextFuzzyRow() {
        moveForwardAndFind(FUZZY_PREDICATE);
    }

    @Override
    public void gotoPrevFuzzyRow() {
        moveBackwardAndFind(FUZZY_PREDICATE);
    }

    @Override
    public void gotoNextNewRow() {
        moveForwardAndFind(NEW_PREDICATE);
    }

    @Override
    public void gotoPrevNewRow() {
        moveBackwardAndFind(NEW_PREDICATE);
    }

    private void moveForwardAndFind(Predicate<TransUnit> condition) {
        if (currentIndex == transUnits.size() - 1) {
            //end of list
            return;
        }
        int nextIndex = currentIndex + 1;
        ListIterator<TransUnit> iterator = transUnits.listIterator(nextIndex);
        while(iterator.hasNext()) {
            TransUnit next = iterator.next();
            if (condition.apply(next)) {
                currentIndex =  nextIndex;
                break;
            }
            nextIndex++;
        }
    }

    private void moveBackwardAndFind(Predicate<TransUnit> condition) {
        ListIterator<TransUnit> iterator = transUnits.listIterator(currentIndex);
        int prevIndex = currentIndex;
        while(iterator.hasPrevious()) {
            TransUnit transUnit = iterator.previous();
            prevIndex--;
            if (condition.apply(transUnit)) {
                currentIndex = prevIndex;
                break;
            }
        }
    }

    public TransUnit getCurrentTransUnit() {
        if (currentIndex >= transUnits.size()) {
            throw new RuntimeException("current index is out of bound.");
        }
        return transUnits.get(currentIndex);
    }

    public List<TransUnit> getTransUnits() {
        return transUnits;
    }

    public boolean moveToIndex(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= transUnits.size()) {
            return false;
        }
        boolean moved = rowIndex != currentIndex;
        currentIndex = rowIndex;
        return moved;
    }
}
