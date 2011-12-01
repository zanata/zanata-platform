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
package org.zanata.webtrans.shared.validation.action;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.shared.model.TransUnit;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public abstract class ValidationAction
{
   private String id;
   private boolean isEnabled;
   
   private final String description;

   private final EventBus eventBus;
   private final TableEditorMessages messages;

   public abstract void execute(TransUnit tu);

   public ValidationAction(String id, String description, final EventBus eventBus, final TableEditorMessages messages)
   {
      this.id = id;
      this.description = description;
      this.eventBus = eventBus;
      this.messages = messages;
   }

   public void displayValidationError(String errorMessage)
   {
      eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyValidationError(getId(), errorMessage)));
   }

   public boolean isEnabled()
   {
      return isEnabled;
   }

   public void setEnabled(boolean isEnabled)
   {
      this.isEnabled = isEnabled;
   }

   public String getId()
   {
      return id;
   }

   public String getDescription()
   {
      return description;
   }
}


 