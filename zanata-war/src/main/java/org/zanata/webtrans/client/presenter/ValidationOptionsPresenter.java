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
package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.RunDocValidationEvent;
import org.zanata.webtrans.client.events.RunDocValidationResultEvent;
import org.zanata.webtrans.client.events.RunDocValidationResultHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.service.ValidationService;
import org.zanata.webtrans.client.view.ValidationOptionsDisplay;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationInfo;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.inject.Inject;

/**
 * 
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class ValidationOptionsPresenter extends WidgetPresenter<ValidationOptionsDisplay> implements ValidationOptionsDisplay.Listener, WorkspaceContextUpdateEventHandler, RunDocValidationResultHandler
{
   private final ValidationService validationService;
   private final WebTransMessages messages;
   private MainView currentView;
   private Set<DocumentId> errorDocs;

   @Inject
   public ValidationOptionsPresenter(ValidationOptionsDisplay display, EventBus eventBus, final ValidationService validationService, final WebTransMessages messages)
   {
      super(display, eventBus);
      this.validationService = validationService;
      this.messages = messages;
   }

   @Override
   protected void onBind()
   {
      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this));
      registerHandler(eventBus.addHandler(RunDocValidationResultEvent.getType(), this));
      initDisplay();

      display.updateValidationResult(null, null);
      display.showReportLink(false);

      display.setListener(this);
   }

   public void initDisplay()
   {
      display.clearValidationSelector();
      ArrayList<ValidationAction> validationActions = new ArrayList<ValidationAction>(validationService.getValidationMap().values());
      for (final ValidationAction validationAction : validationActions)
      {
         ValidationInfo validationInfo = validationAction.getValidationInfo();

         HasValueChangeHandlers<Boolean> changeHandler = display.addValidationSelector(validationAction.getId().getDisplayName(), validationAction.getDescription(), validationInfo.isEnabled(), validationInfo.isLocked());
         changeHandler.addValueChangeHandler(new ValidationOptionValueChangeHandler(validationAction));
      }
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   protected void onRevealDisplay()
   {
   }

   class ValidationOptionValueChangeHandler implements ValueChangeHandler<Boolean>
   {
      private final ValidationAction validationAction;

      public ValidationOptionValueChangeHandler(ValidationAction validationAction)
      {
         this.validationAction = validationAction;
      }

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event)
      {
         validationService.updateStatus(validationAction.getId(), event.getValue());
         if (event.getValue())
         {
            for (ValidationAction excluded : validationAction.getExclusiveValidations())
            {
               validationService.updateStatus(excluded.getId(), false);
               display.changeValidationSelectorValue(excluded.getId().getDisplayName(), false);
            }
         }
      }
   }

   @Override
   public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
   {
      validationService.setValidationRules(event.getValidationInfoList());

      initDisplay();
   }

   public void setCurrentView(MainView view)
   {
      currentView = view;
      if (view == MainView.Documents)
      {
         display.setRunValidationVisible(true);
         display.setRunValidationTitle(messages.documentValidationTitle());
         if(hasErrorReport())
         {
            display.showReportLink(true);
         }
      }
      else
      {
         display.setRunValidationVisible(false);
         display.setRunValidationTitle(messages.editorValidationTitle());
         display.showReportLink(false);
      }
   }

   @Override
   public void onRunValidation()
   {
      eventBus.fireEvent(new RunDocValidationEvent(currentView));
   }

   @Override
   public void onCompleteRunDocValidation(RunDocValidationResultEvent event)
   {
      display.updateValidationResult(event.getStartTime(), event.getEndTime());
      errorDocs = event.getErrorDocs();
      if (currentView == MainView.Documents)
      {
         display.showReportLink(true);
      }
   }

   @Override
   public void onRequestValidationReport()
   {
      // display.showReportPopup(errorDocs);
      // run RPC call, by each docs
   }
   
   private boolean hasErrorReport()
   {
      return errorDocs != null && !errorDocs.isEmpty();
   }
}


 