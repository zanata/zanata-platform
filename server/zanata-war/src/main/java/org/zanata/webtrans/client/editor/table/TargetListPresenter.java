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

import com.google.inject.Inject;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import org.zanata.webtrans.shared.model.TransUnit;

public class TargetListPresenter extends WidgetPresenter<TargetListDisplay> {

    private TargetListDisplay display;
    private TransUnit transUnit;

    @Inject
    public TargetListPresenter(TargetListDisplay display, EventBus eventBus) {
        super(display, eventBus);
        this.display = display;
    }

    public void setTransUnit(TransUnit transUnit) {
        this.transUnit = transUnit;
        display.setTargets(transUnit.getTargets());
    }

    @Override
    protected void onBind() {
        //TODO implement
        //
    }

    @Override
    protected void onUnbind() {
    }

    @Override
    protected void onRevealDisplay() {
    }
}
