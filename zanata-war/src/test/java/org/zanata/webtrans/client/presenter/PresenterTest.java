/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import org.easymock.Capture;

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Provides common functionality for testing presenter classes.
 * 
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
public abstract class PresenterTest
{
   private ArrayList<Object> allMocks = new ArrayList<Object>();;
   private List<Capture<?>> allCaptures = new ArrayList<Capture<?>>();

   /**
    * Convenience method to add a capture to allCaptures
    * 
    * @return the given capture for assignment
    */
   protected <T> Capture<T> addCapture(Capture<T> capture)
   {
      allCaptures.add(capture);
      return capture;
   }

   /**
    * Create a mock, adding it to the list used in {@link #replayAllMocks()},
    * {@link #resetAllMocks()} and {@link #verifyAllMocks()}.
    * 
    * @param clazz class to mock
    * @return the created mock
    */
   protected <T> T createAndAddMock(Class<T> clazz)
   {
      T mock = createMock(clazz);
      allMocks.add(mock);
      return mock;
   }

   /**
    * @return a mock handler registration with no defined behaviour.
    */
   protected HandlerRegistration mockHandlerRegistration()
   {
      return createMock(HandlerRegistration.class);
   }

   protected void resetAllCaptures()
   {
      for (Capture<?> capture : allCaptures)
      {
         capture.reset();
      }
   }

   protected void replayAllMocks()
   {
      replay(allMocks.toArray());
   }

   protected void resetAllMocks()
   {
      reset(allMocks.toArray());
   }

   protected void verifyAllMocks()
   {
      verify(allMocks.toArray());
   }

   /**
    * Called with {@link #resetAll()}.
    */
   protected abstract void setDefaultBindExpectations();

   /**
    * Reset all mocks and captures, and setup default expectations.
    */
   protected void resetAll()
   {
      resetAllMocks();
      resetAllCaptures();
      setDefaultBindExpectations();
   }
}
