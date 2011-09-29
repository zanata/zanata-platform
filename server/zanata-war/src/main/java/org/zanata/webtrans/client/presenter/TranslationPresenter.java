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
package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationPresenter extends WidgetPresenter<TranslationPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      void setEditorView(Widget editorView);

      void setSidePanel(Widget sidePanel);

      void setTranslationMemoryView(Widget translationMemoryView);

      void setGlossaryView(Widget glossaryView);

      void setTmViewVisible(boolean visible);

      HasClickHandlers getHideTMViewButton();

      HasClickHandlers getShowTMViewButton();

      void setShowTMViewButtonVisible(boolean visible);

      void setSidePanelViewVisible(boolean visible);

      HasClickHandlers getHideSidePanelViewButton();

      HasClickHandlers getShowSidePanelViewButton();

      void setShowSidePanelViewButtonVisible(boolean visible);
   }

   private final TranslationEditorPresenter translationEditorPresenter;
   private final SidePanelPresenter sidePanelPresenter;
   private final TransMemoryPresenter transMemoryPresenter;
   private final GlossaryPresenter glossaryPresenter;

   @Inject
   public TranslationPresenter(Display display, EventBus eventBus, final TranslationEditorPresenter translationEditorPresenter, final SidePanelPresenter sidePanelPresenter, final TransMemoryPresenter transMemoryPresenter, final GlossaryPresenter glossaryPresenter)
   {
      super(display, eventBus);
      this.translationEditorPresenter = translationEditorPresenter;
      this.transMemoryPresenter = transMemoryPresenter;
      this.sidePanelPresenter = sidePanelPresenter;
      this.glossaryPresenter = glossaryPresenter;
   }

   @Override
   public void onRevealDisplay()
   {
   }

   @Override
   protected void onBind()
   {
      transMemoryPresenter.bind();
      display.setTranslationMemoryView(transMemoryPresenter.getDisplay().asWidget());

      glossaryPresenter.bind();
      display.setGlossaryView(glossaryPresenter.getDisplay().asWidget());

      translationEditorPresenter.bind();
      display.setEditorView(translationEditorPresenter.getDisplay().asWidget());

      sidePanelPresenter.bind();
      display.setSidePanel(sidePanelPresenter.getDisplay().asWidget());

      registerHandler(display.getHideSidePanelViewButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.setSidePanelViewVisible(false);
            // sidePanelPresenter.unbind();
            // translationEditorPresenter.unbind();
            display.setShowSidePanelViewButtonVisible(true);
         }
      }));

      display.setShowSidePanelViewButtonVisible(false);
      display.getShowSidePanelViewButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            // sidePanelPresenter.bind();
            // translationEditorPresenter.bind();
            display.setSidePanelViewVisible(true);
            display.setShowSidePanelViewButtonVisible(false);
         }
      });

      registerHandler(display.getHideTMViewButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.setTmViewVisible(false);
            transMemoryPresenter.unbind();
            glossaryPresenter.unbind();
            display.setShowTMViewButtonVisible(true);
         }
      }));

      display.setShowTMViewButtonVisible(false);
      display.getShowTMViewButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            transMemoryPresenter.bind();
            glossaryPresenter.bind();

            display.setTmViewVisible(true);
            display.setShowTMViewButtonVisible(false);
            TransUnit tu = translationEditorPresenter.getSelectedTransUnit();
            if (tu != null)
            {
               transMemoryPresenter.showResultsFor(tu);
            }
         }
      });
   }

   @Override
   protected void onUnbind()
   {
      transMemoryPresenter.unbind();
      glossaryPresenter.unbind();
      translationEditorPresenter.unbind();
      sidePanelPresenter.unbind();
   }

   public void saveEditorPendingChange()
   {
      translationEditorPresenter.saveEditorPendingChange();
   }

}
