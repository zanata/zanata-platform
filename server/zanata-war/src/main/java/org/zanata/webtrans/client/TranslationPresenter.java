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
package org.zanata.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.editor.CheckKey;
import org.zanata.webtrans.client.editor.CheckKeyImpl;
import org.zanata.webtrans.client.editor.filter.TransFilterPresenter;
import org.zanata.webtrans.shared.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationPresenter extends WidgetPresenter<TranslationPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      void setEditorView(Widget editorView);

      void setSidePanel(Widget sidePanel);

      void setTranslationMemoryView(Widget translationMemoryView);

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

   private TransFilterPresenter.Display transFilterView;

   @Inject
   public TranslationPresenter(Display display, EventBus eventBus, final TranslationEditorPresenter translationEditorPresenter, final SidePanelPresenter sidePanelPresenter, final TransMemoryPresenter transMemoryPresenter)
   {
      super(display, eventBus);
      this.translationEditorPresenter = translationEditorPresenter;
      this.transMemoryPresenter = transMemoryPresenter;
      this.sidePanelPresenter = sidePanelPresenter;
   }

   @Override
   public void onRevealDisplay()
   {
   }

   public void bind(TransFilterPresenter.Display transFilterView)
   {
      this.transFilterView = transFilterView;
      super.bind();
   }

   @Override
   protected void onBind()
   {
      transMemoryPresenter.bind();
      display.setTranslationMemoryView(transMemoryPresenter.getDisplay().asWidget());

      translationEditorPresenter.bind(transFilterView);
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
            display.setTmViewVisible(true);
            display.setShowTMViewButtonVisible(false);
            TransUnit tu = translationEditorPresenter.getSelectedTransUnit();
            if (tu != null)
            {
               transMemoryPresenter.showResultsFor(tu);
            }
         }
      });

      final CheckKey checkKey = new CheckKeyImpl(CheckKeyImpl.Context.Navigation);

      Event.addNativePreviewHandler(new NativePreviewHandler()
      {
         @Override
         public void onPreviewNativeEvent(NativePreviewEvent event)
         {
            /**
             * @formatter:off
             * keyup is used because TargetCellEditor will intercept the event
             * again (Firefox) See textArea.addKeyDownHandler@InlineTargetCellEditor
             * 
             * Only when the Table is showed,editor is closed, search field not
             * focused, the keyboard event will be processed.
             **/
            if (display.asWidget().isVisible() && 
                  !translationEditorPresenter.isTargetCellEditorFocused() && 
                  !transFilterView.isFocused() && 
                  !transMemoryPresenter.getDisplay().isFocused() && 
                  !translationEditorPresenter.getDisplay().isPagerFocused())
            {
               //@formatter:on
               checkKey.init(event.getNativeEvent());

               if (event.getNativeEvent().getType().equals("keyup"))
               {
                  if (checkKey.isCopyFromSourceKey())
                  {
                     if (translationEditorPresenter.getSelectedTransUnit() != null)
                     {
                        Log.info("Copy from source");
                        stopDefaultAction(event);
                        translationEditorPresenter.gotoCurrentRow();
                        translationEditorPresenter.cloneAction();
                     }
                  }
                  else if (checkKey.isEnterKey() && !checkKey.isCtrlKey())
                  {
                     if (translationEditorPresenter.getSelectedTransUnit() != null)
                     {
                        if (!translationEditorPresenter.isCancelButtonFocused())
                        {
                           Log.info("open editor");
                           stopDefaultAction(event);
                           translationEditorPresenter.gotoCurrentRow();
                        }
                        translationEditorPresenter.setCancelButtonFocused(false);
                     }
                  }
               }
               if (event.getNativeEvent().getType().equals("keydown"))
               {
                  if (checkKey.isPreviousEntryKey())
                  {
                     Log.info("Go to previous entry");
                     stopDefaultAction(event);
                     translationEditorPresenter.gotoPrevRow(false);
                  }
                  else if (checkKey.isNextEntryKey())
                  {
                     Log.info("Go to next entry");
                     stopDefaultAction(event);
                     translationEditorPresenter.gotoNextRow(false);
                  }
               }
            }
         }

         public void stopDefaultAction(NativePreviewEvent event)
         {
            event.cancel();
            event.getNativeEvent().stopPropagation();
            event.getNativeEvent().preventDefault();
         }
      });
   }

   @Override
   protected void onUnbind()
   {
      transMemoryPresenter.unbind();
      translationEditorPresenter.unbind();
      sidePanelPresenter.unbind();
   }

   public void saveEditorPendingChange()
   {
      translationEditorPresenter.saveEditorPendingChange();
   }
}
