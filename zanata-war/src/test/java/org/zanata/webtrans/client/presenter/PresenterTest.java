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

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.ui.HasValue;

/**
 * Provides common functionality for testing presenter classes.
 * 
 * TODO add overview of usage
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
    * Override to reset non-mock objects. Called with {@link #resetAll()}
    * before {@link #setDefaultBindExpectations()}.
    */
   protected void resetTestObjects()
   {
      // TODO could make abstract, but some tests will not require this.
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
      resetTestObjects();
      setDefaultBindExpectations();
   }

   protected void simulateClick(Capture<ClickHandler> clickable)
   {
      ClickEvent event = new ClickEvent()
      {
      };
      clickable.getValue().onClick(event);
   }

   protected <T> void valueChangeEvent(Capture<ValueChangeHandler<T>> handler, T newValue)
   {
      handler.getValue().onValueChange(new ValueChangeEvent<T>(newValue)
      {
      });
   }

   /**
    * Expect a single handler registration on a mock object, and capture the
    * click handler in the given {@link Capture}
    * 
    * @param mockObjectToClick
    * @param captureForHandler
    */
   protected void expectClickHandlerRegistration(HasClickHandlers mockObjectToClick, Capture<ClickHandler> captureForHandler)
   {
      expect(mockObjectToClick.addClickHandler(and(capture(captureForHandler), isA(ClickHandler.class)))).andReturn(mockHandlerRegistration()).once();
   }

   /**
    * Expects an event handler to be registered against the given event bus,
    * capturing the handler in the given capture.
    * 
    * @param mockEventBus
    * @param expectedType
    * @param expectedClass
    * @param handlerCapture
    */
   protected <H extends EventHandler> void expectEventHandlerRegistration(EventBus mockEventBus, Type<H> expectedType, Class<H> expectedClass, Capture<H> handlerCapture)
   {
      expect(mockEventBus.addHandler(eq(expectedType), and(capture(handlerCapture), isA(expectedClass)))).andReturn(mockHandlerRegistration()).once();
   }

   protected <T> void expectValueChangeHandlerRegistration(HasValue<T> mockObjectWithValue, Capture<ValueChangeHandler<T>> captureForHandler)
   {
      expect(mockObjectWithValue.addValueChangeHandler(capture(captureForHandler))).andReturn(mockHandlerRegistration()).once();
   }

}
