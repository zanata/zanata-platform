/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.presenter;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.editor.filter.TransFilterPresenter;
import org.zanata.webtrans.client.events.ButtonDisplayChangeEvent;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.resources.EditorConfigConstants;
import org.zanata.webtrans.client.ui.EditorOptionsPanel;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SidePanelPresenter extends WidgetPresenter<SidePanelPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      void setValidationOptionsView(Widget widget);

      void setEditorOptionsPanel(Widget widget);
   }

   private final ValidationOptionsPresenter validationOptionsPresenter;
   private final EditorOptionsPanel editorOptionsPanel;

   private Map<String, Boolean> configMap = new HashMap<String, Boolean>();
   private final WorkspaceContext workspaceContext;

   @Inject
   public SidePanelPresenter(final Display display, final EventBus eventBus, final ValidationOptionsPresenter validationDetailsPresenter, final TransFilterPresenter transFilterPresenter, final WorkspaceContext workspaceContext)
   {
      super(display, eventBus);
      this.editorOptionsPanel = new EditorOptionsPanel();
      this.validationOptionsPresenter = validationDetailsPresenter;
      this.workspaceContext = workspaceContext;

      configMap.put(EditorConfigConstants.BUTTON_ENTER, false);
      configMap.put(EditorConfigConstants.BUTTON_ESC, false);
      configMap.put(EditorConfigConstants.BUTTON_FUZZY, true);
      configMap.put(EditorConfigConstants.BUTTON_UNTRANSLATED, true);
   }

   private final ValueChangeHandler<Boolean> filterChangeHandler = new ValueChangeHandler<Boolean>()
   {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event)
      {
         eventBus.fireEvent(new FilterViewEvent(editorOptionsPanel.getTranslatedChk().getValue(), editorOptionsPanel.getNeedReviewChk().getValue(), editorOptionsPanel.getUntranslatedChk().getValue(), false));
      }
   };


   @Override
   protected void onBind()
   {
      validationOptionsPresenter.bind();
      display.setValidationOptionsView(validationOptionsPresenter.getDisplay().asWidget());
      display.setEditorOptionsPanel(editorOptionsPanel);
      if (workspaceContext.isReadOnly())
      {
         setReadOnly(true);
      }

      registerHandler(editorOptionsPanel.getTranslatedChk().addValueChangeHandler(filterChangeHandler));
      registerHandler(editorOptionsPanel.getNeedReviewChk().addValueChangeHandler(filterChangeHandler));
      registerHandler(editorOptionsPanel.getUntranslatedChk().addValueChangeHandler(filterChangeHandler));

      registerHandler(eventBus.addHandler(FilterViewEvent.getType(), new FilterViewEventHandler()
      {
         @Override
         public void onFilterView(FilterViewEvent event)
         {
            if (event.isCancelFilter())
            {
               editorOptionsPanel.getTranslatedChk().setValue(event.isFilterTranslated(), false);
               editorOptionsPanel.getNeedReviewChk().setValue(event.isFilterNeedReview(), false);
               editorOptionsPanel.getUntranslatedChk().setValue(event.isFilterUntranslated(), false);
            }

            // if filter view, hide model navigation
            boolean showingFullList = (event.isFilterTranslated() == event.isFilterNeedReview()) && (event.isFilterTranslated() == event.isFilterUntranslated());
            if (showingFullList)
            {
               editorOptionsPanel.setNavOptionVisible(true);
            }
            else
            {
               editorOptionsPanel.setNavOptionVisible(false);
            }
         }
      }));

      registerHandler(editorOptionsPanel.getEditorButtonsChk().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            Log.info("Show editor buttons: " + event.getValue());
            eventBus.fireEvent(new ButtonDisplayChangeEvent(event.getValue()));
         }
      }));

      registerHandler(editorOptionsPanel.getEnterChk().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            Log.info("Enable 'Enter' Key to save and move to next string: " + event.getValue());
            configMap.put(EditorConfigConstants.BUTTON_ENTER, event.getValue());
            eventBus.fireEvent(new UserConfigChangeEvent(configMap));
         }
      }));

      registerHandler(editorOptionsPanel.getEscChk().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            Log.info("Enable 'Esc' Key to close editor: " + event.getValue());
            configMap.put(EditorConfigConstants.BUTTON_ESC, event.getValue());
            eventBus.fireEvent(new UserConfigChangeEvent(configMap));
         }
      }));

      registerHandler(editorOptionsPanel.getOptionsList().addChangeHandler(new ChangeHandler()
      {
         @Override
         public void onChange(ChangeEvent event)
         {
            String selectedOption = editorOptionsPanel.getOptionsList().getItemText(editorOptionsPanel.getOptionsList().getSelectedIndex());
            if (selectedOption.equals(EditorConfigConstants.OPTION_FUZZY_UNTRANSLATED))
            {
               configMap.put(EditorConfigConstants.BUTTON_UNTRANSLATED, true);
               configMap.put(EditorConfigConstants.BUTTON_FUZZY, true);
            }
            else if (selectedOption.equals(EditorConfigConstants.OPTION_FUZZY))
            {
               configMap.put(EditorConfigConstants.BUTTON_FUZZY, true);
               configMap.put(EditorConfigConstants.BUTTON_UNTRANSLATED, false);
            }
            else if (selectedOption.equals(EditorConfigConstants.OPTION_UNTRANSLATED))
            {
               configMap.put(EditorConfigConstants.BUTTON_FUZZY, false);
               configMap.put(EditorConfigConstants.BUTTON_UNTRANSLATED, true);
            }
            eventBus.fireEvent(new UserConfigChangeEvent(configMap));
         }
      }));
      
      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), new WorkspaceContextUpdateEventHandler()
      {
         @Override
         public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
         {
            setReadOnly(event.isReadOnly());
         }
      }));
   }

   void setReadOnly(boolean readOnly)
   {
      if (readOnly)
      {
         eventBus.fireEvent(new ButtonDisplayChangeEvent(false));
      }
      else
      {
         eventBus.fireEvent(new ButtonDisplayChangeEvent(editorOptionsPanel.getEditorButtonsChk().getValue()));
      }

      boolean active = !readOnly;
      editorOptionsPanel.getEditorButtonsChk().setEnabled(active);
      editorOptionsPanel.getEnterChk().setEnabled(active);
      editorOptionsPanel.getEscChk().setEnabled(active);

      validationOptionsPresenter.setEnabled(active);
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

}
