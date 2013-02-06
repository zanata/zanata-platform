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
import java.util.Collections;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.service.ValidationService;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationObject;
import org.zanata.webtrans.shared.validation.ValidationFactory;

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
public class ValidationOptionsPresenter extends WidgetPresenter<ValidationOptionsPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      HasValueChangeHandlers<Boolean> addValidationSelector(String label, String tooltip, boolean enabled);

      void changeValidationSelectorValue(String label, boolean enabled);
   }

   private final ValidationService validationService;

   @Inject
   public ValidationOptionsPresenter(Display display, EventBus eventBus, final ValidationService validationService)
   {
      super(display, eventBus);
      this.validationService = validationService;
   }

   @Override
   protected void onBind()
   {
      ArrayList<ValidationAction> validationActions = new ArrayList<ValidationAction>(validationService.getValidationMap().values());
      Collections.sort(validationActions, ValidationFactory.ObjectComparator);
      for (final ValidationAction validationAction : validationActions)
      {
         HasValueChangeHandlers<Boolean> changeHandler = display.addValidationSelector(validationAction.getValidationInfo().getId().getDisplayName(), validationAction.getValidationInfo().getDescription(), validationAction.getValidationInfo().isEnabled());
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
         validationService.updateStatus(validationAction.getValidationInfo().getId(), event.getValue());
         if (event.getValue())
         {
            for (ValidationObject excluded : validationAction.getExclusiveValidations())
            {
               validationService.updateStatus(excluded.getValidationInfo().getId(), false);
               display.changeValidationSelectorValue(excluded.getValidationInfo().getId().getDisplayName(), false);
            }
         }
      }
   }
}


 