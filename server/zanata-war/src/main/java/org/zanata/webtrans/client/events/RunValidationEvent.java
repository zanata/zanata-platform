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
package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.gwt.event.shared.GwtEvent;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class RunValidationEvent extends GwtEvent<RunValidationEventHandler>
{
   /**
    * Handler type.
    */
   private static Type<RunValidationEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<RunValidationEventHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<RunValidationEventHandler>());
   }

   private String source, target;
   private TransUnitId id;
   private boolean fireNotification = true;

   public RunValidationEvent(TransUnitId id, String source, String target)
   {
      this.id = id;
      this.source = source;
      this.target = target;
   }

   public RunValidationEvent(TransUnitId id, String source, String target, boolean fireNotification)
   {
      this.id = id;
      this.source = source;
      this.target = target;
      this.fireNotification = fireNotification;
   }

   @Override
   public Type<RunValidationEventHandler> getAssociatedType()
   {
      return getType();
   }


   @Override
   protected void dispatch(RunValidationEventHandler handler)
   {
      handler.onValidate(this);
   }

   public String getSource()
   {
      return source;
   }

   public String getTarget()
   {
      return target;
   }

   public TransUnitId getId()
   {
      return id;
   }

   public boolean isFireNotification()
   {
      return fireNotification;
   }
}


 