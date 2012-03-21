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

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.customware.gwt.presenter.client.EventBus;
import org.zanata.webtrans.client.presenter.SourcePanelPresenter;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Singleton
public class TargetContentsPresenter implements TargetContentsDisplay.Listener {

    private TargetContentsDisplay currentDisplay;
    private Provider<TargetContentsDisplay> displayProvider;
    private EventBus eventBus;
    private SourcePanelPresenter sourcePanelPresenter;
    private Map<TransUnit, TargetContentsDisplay> displays;
    private List<TargetContentsDisplay> displayList;
    private Iterator<TargetContentsDisplay> displayIterator;
    private List<ToggleEditor> currentEditors;

    @Inject
    public TargetContentsPresenter(Provider<TargetContentsDisplay> displayProvider, EventBus eventBus, SourcePanelPresenter sourcePanelPresenter) {
        this.displayProvider = displayProvider;
        this.eventBus = eventBus;
        this.sourcePanelPresenter = sourcePanelPresenter;

        displays = new HashMap<TransUnit, TargetContentsDisplay>();
    }

    boolean isEditing() {
        return currentDisplay.isEditing();
    }

    public void setToViewMode() {
        if (currentDisplay != null) {
            currentDisplay.setToView();
        }
    }

    public void setCurrentEditorText(String text) {
        if (currentDisplay != null) {
            currentDisplay.getCurrentEditor().setText(text);
        }
    }

    public void insertTextInCursorPosition(String text) {
        //TODO implement
        //throw new UnsupportedOperationException("Implement me!");
    }

    public void showEditors(TransUnit cellValue) {
        currentDisplay = displays.get(cellValue);
        currentDisplay.getCurrentEditor().setViewMode(ToggleEditor.ViewMode.EDIT);
    }

    public TargetContentsDisplay getNextTargetContentsDisplay(TransUnit transUnit) {
        TargetContentsDisplay result;
        if (!displayIterator.hasNext()) {
            Log.info("over one page's widget. going to another page? resetting iterator");
            displayIterator = displayList.iterator();
        }
        result = displayIterator.next();
        currentEditors = result.setTargets(transUnit.getTargets());
        currentDisplay = result;
        return result;
    }

    public void initWidgets(int pageSize) {
        displayList = Lists.newArrayList();
        for (int i = 0; i < pageSize; i++) {
            TargetContentsDisplay display = displayProvider.get();
            display.setListener(this);
            displayList.add(display);
            displayIterator = displayList.iterator();
        }
    }

    public TargetContentsDisplay getCurrentDisplay() {
        return currentDisplay;
    }

    @Override
    public void validate(ToggleEditor editor) {
//        eventBus.fireEvent(new RunValidationEvent(id, ));
    }

    @Override
    public void saveAsApproved(ToggleEditor editor) {
        //TODO we should probably get new value out and save
        editor.setViewMode(ToggleEditor.ViewMode.VIEW);
        int currentIndex = currentEditors.indexOf(editor);
        if (currentIndex + 1 < currentEditors.size()) {
            currentEditors.get(currentIndex + 1).setViewMode(ToggleEditor.ViewMode.EDIT);
        }
        //TODO if it's out of current editor index, we should move to next row
    }

    @Override
    public void copySource(ToggleEditor editor) {
        editor.setText(sourcePanelPresenter.getSelectedSource());
        editor.autoSize();
    }
}
