/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package net.openl10n.flies.webtrans.client;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class TranslationPresenter extends WidgetPresenter<TranslationPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      void setEditorView(Widget editorView);

      void setSidePanel(Widget sidePanel);

   }

   public static final Place PLACE = new Place("TranslationView");
   private final TranslationEditorPresenter translationEditorPresenter;
   private final SidePanelPresenter sidePanelPresenter;

   @Inject
   public TranslationPresenter(Display display, EventBus eventBus, final TranslationEditorPresenter translationEditorPresenter, final SidePanelPresenter sidePanelPresenter)
   {
      super(display, eventBus);
      this.translationEditorPresenter = translationEditorPresenter;
      this.sidePanelPresenter = sidePanelPresenter;
   }

   @Override
   public void refreshDisplay()
   {
   }

   @Override
   public void revealDisplay()
   {
   }

   @Override
   protected void onBind()
   {
      translationEditorPresenter.bind();
      display.setEditorView(translationEditorPresenter.getDisplay().asWidget());

      sidePanelPresenter.bind();
      display.setSidePanel(sidePanelPresenter.getDisplay().asWidget());
   }

   @Override
   protected void onUnbind()
   {
      translationEditorPresenter.unbind();
      sidePanelPresenter.unbind();
   }

   @Override
   public Place getPlace()
   {
      return null;
   }

   @Override
   protected void onPlaceRequest(PlaceRequest request)
   {
   }

}
