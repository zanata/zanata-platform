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
package org.zanata.webtrans.client.ui;

import java.util.List;
import java.util.Map;

import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.ValidationAction;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class ValidationWarningPanel extends ShortcutContextAwareDialogBox implements ValidationWarningDisplay
{
   private static ValidationWarningPanelUiBinder uiBinder = GWT.create(ValidationWarningPanelUiBinder.class);

   interface ValidationWarningPanelUiBinder extends UiBinder<HTMLPanel, ValidationWarningPanel>
   {
   }

   private TransUnitId transUnitId;

   private int editorIndex;

   private TargetContentsDisplay.Listener listener;

   @UiField
   UnorderedListWidget translations;

   @UiField
   UnorderedListWidget errorList;

   @UiField(provided = true)
   Button returnToEditor;

   @UiField(provided = true)
   Button saveAsFuzzy;

   @Inject
   public ValidationWarningPanel(TableEditorMessages messages, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(false, true, ShortcutContext.ValidationWarningPopup, keyShortcutPresenter);

      setStyleName("new-zanata");

      returnToEditor = new Button(messages.returnToEditor());
      saveAsFuzzy = new Button(messages.saveAsFuzzy());

      HTMLPanel container = uiBinder.createAndBindUi(this);

      setGlassEnabled(true);
      setWidget(container);
      hide();
   }

   public void setListener(TargetContentsDisplay.Listener listener)
   {
      this.listener = listener;
      addListenerToButtons();
   }

   private void addListenerToButtons()
   {
      saveAsFuzzy.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            hide();
            listener.saveAsFuzzy(transUnitId);
         }
      });
      returnToEditor.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            hide();
            listener.onEditorClicked(transUnitId, editorIndex);
         }
      });
   }

   @Override
   public void center(TransUnitId transUnitId, int editorIndex, List<String> targets, Map<ValidationAction, List<String>> errorMessages)
   {
      this.transUnitId = transUnitId;
      this.editorIndex = editorIndex;
      refreshView(targets, errorMessages);
      center();
   }

   private void refreshView(List<String> targets, Map<ValidationAction, List<String>> errorMessages)
   {
      translations.clear();
      errorList.clear();

      for (String target : targets)
      {
         SafeHtmlBuilder builder = new SafeHtmlBuilder();
         builder.append(TextContentsDisplay.asSyntaxHighlight(Lists.newArrayList(target)).toSafeHtml());
         translations.add(new HTMLPanel("li", builder.toSafeHtml().asString()));
      }

      for (List<String> messages : errorMessages.values())
      {
         for (String message : messages)
         {
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder.appendEscaped(message);
            errorList.add(new HTMLPanel("li", builder.toSafeHtml().asString()));
         }
      }
   }
}
