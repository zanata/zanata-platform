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

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.editor.filter.TransFilterPresenter;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.EditorOptionsPanel;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SidePanelPresenter extends WidgetPresenter<SidePanelPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      void setValidationOptionsView(Widget widget);

      void setEditorOptionsPanel(Widget widget);
   }

   private final DispatchAsync dispatcher;
   private final ValidationOptionsPresenter validationOptionsPresenter;
   private final EditorOptionsPanel editorOptionsPanel;
   
   @Inject
   public SidePanelPresenter(final Display display, final EventBus eventBus, CachingDispatchAsync dispatcher, final ValidationOptionsPresenter validationDetailsPresenter, final TransFilterPresenter transFilterPresenter)
   {
      super(display, eventBus);
      this.editorOptionsPanel = new EditorOptionsPanel(eventBus);
      this.validationOptionsPresenter = validationDetailsPresenter;
      this.dispatcher = dispatcher;
   }

   @Override
   protected void onBind()
   {
      validationOptionsPresenter.bind();
      display.setValidationOptionsView(validationOptionsPresenter.getDisplay().asWidget());

      display.setEditorOptionsPanel(editorOptionsPanel);
   }



   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

}
