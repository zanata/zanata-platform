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

import org.zanata.webtrans.client.view.OptionsDisplay;

import com.google.inject.Inject;

public class OptionsPresenter extends WidgetPresenter<OptionsDisplay> implements OptionsDisplay.Listener
{
   private final EditorOptionsPresenter editorOptionsPresenter;

   @Inject
   public OptionsPresenter(OptionsDisplay display, EventBus eventBus, EditorOptionsPresenter editorOptionsPresenter)
   {
      super(display, eventBus);
      this.editorOptionsPresenter = editorOptionsPresenter;
      display.setListener(this);
   }
   
   public void setOptionsView(MainView view)
   {
      // optionContainer.clear();
      // switch (view)
      // {
      // case Editor:
      // optionContainer.add(editorOptionView.asWidget());
      // break;
      // case Search:
      // break;
      // case Documents:
      // default:
      // break;
      // }

      display.setOptions(editorOptionsPresenter.getDisplay().asWidget());
   }

   @Override
   protected void onBind()
   {
      editorOptionsPresenter.onBind();
   }

   @Override
   public void onShowErrorsOptionChanged(Boolean showErrorChkValue)
   {
      editorOptionsPresenter.onShowErrorsOptionChanged(showErrorChkValue);
   }

   @Override
   protected void onUnbind()
   {
      editorOptionsPresenter.unbind();
   }

   @Override
   public void onRevealDisplay()
   {
   }

   @Override
   public void persistOptionChange()
   {
      editorOptionsPresenter.persistOptionChange();
   }

   @Override
   public void loadOptions()
   {
      editorOptionsPresenter.loadOptions();
   }

   @Override
   public void loadDefaultOptions()
   {
      editorOptionsPresenter.loadDefaultOptions();
   }

}
