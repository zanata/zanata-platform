/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.view.OptionsDisplay;

import com.google.inject.Inject;

public class OptionsPresenter extends WidgetPresenter<OptionsDisplay> implements OptionsDisplay.Listener, UserConfigChangeHandler
{
   private final EditorOptionsPresenter editorOptionsPresenter;
   private final DocumentListOptionsPresenter documentListOptionsPresenter;
   private MainView currentOptionsView;
   private final UserConfigHolder configHolder;


   @Inject
   public OptionsPresenter(OptionsDisplay display, EventBus eventBus, EditorOptionsPresenter editorOptionsPresenter, DocumentListOptionsPresenter documentListOptionsPresenter, UserConfigHolder configHolder)
   {
      super(display, eventBus);
      this.editorOptionsPresenter = editorOptionsPresenter;
      this.documentListOptionsPresenter = documentListOptionsPresenter;
      this.configHolder = configHolder;
      display.setListener(this);
   }

   public void setOptionsView(MainView view)
   {
      currentOptionsView = view;
      switch (view)
      {
      case Editor:
         display.setOptions(editorOptionsPresenter.getDisplay().asWidget());
         break;
      case Search:
         display.setOptions(null);
         break;
      case Documents:
      default:
         display.setOptions(documentListOptionsPresenter.getDisplay().asWidget());
         break;
      }

   }

   @Override
   protected void onBind()
   {
      editorOptionsPresenter.onBind();
      documentListOptionsPresenter.onBind();

      registerHandler(eventBus.addHandler(UserConfigChangeEvent.TYPE, this));
   }

   @Override
   public void onShowErrorsOptionChanged(Boolean showErrorChkValue)
   {
      switch (currentOptionsView)
      {
      case Editor:
         editorOptionsPresenter.onShowErrorsOptionChanged(showErrorChkValue);
         break;
      case Search:
         break;
      case Documents:
      default:
         documentListOptionsPresenter.onShowErrorsOptionChanged(showErrorChkValue);
         break;
      }
   }

   @Override
   protected void onUnbind()
   {
      editorOptionsPresenter.unbind();
      documentListOptionsPresenter.unbind();
   }

   @Override
   public void onRevealDisplay()
   {
   }

   @Override
   public void persistOptionChange()
   {
      switch (currentOptionsView)
      {
      case Editor:
         editorOptionsPresenter.persistOptionChange();
         break;
      case Search:
         break;
      case Documents:
      default:
         documentListOptionsPresenter.persistOptionChange();
         break;
      }
   }

   @Override
   public void loadOptions()
   {
      switch (currentOptionsView)
      {
      case Editor:
         editorOptionsPresenter.loadOptions();
         break;
      case Search:
         break;
      case Documents:
      default:
         documentListOptionsPresenter.loadOptions();
         break;
      }
   }

   @Override
   public void loadDefaultOptions()
   {
      switch (currentOptionsView)
      {
      case Editor:
         editorOptionsPresenter.loadDefaultOptions();
         break;
      case Search:
         break;
      case Documents:
      default:
         documentListOptionsPresenter.loadDefaultOptions();
         break;
      }
   }

   @Override
   public void onUserConfigChanged(UserConfigChangeEvent event)
   {
      display.setShowErrorChk(configHolder.isShowError());
   }
}
