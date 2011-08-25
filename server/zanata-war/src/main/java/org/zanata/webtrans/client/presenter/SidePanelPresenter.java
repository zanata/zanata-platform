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
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.editor.filter.TransFilterPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SidePanelPresenter extends WidgetPresenter<SidePanelPresenter.Display>
{

   public interface Display extends WidgetDisplay
   {
      void setWorkspaceUsersView(Widget widget);

      void setTransUnitDetailView(Widget widget);

      void collapseUsersPanel();

      void expandUsersPanel();
   }

   private final WorkspaceUsersPresenter workspaceUsersPresenter;
   private final TransUnitDetailsPresenter transUnitDetailsPresenter;

   @Inject
   public SidePanelPresenter(final Display display, final EventBus eventBus, final TransUnitDetailsPresenter transUnitDetailsPresenter, final WorkspaceUsersPresenter workspaceUsersPresenter, final TransFilterPresenter transFilterPresenter)
   {
      super(display, eventBus);
      this.workspaceUsersPresenter = workspaceUsersPresenter;
      this.transUnitDetailsPresenter = transUnitDetailsPresenter;
   }

   @Override
   protected void onBind()
   {

      transUnitDetailsPresenter.bind();
      display.setTransUnitDetailView(transUnitDetailsPresenter.getDisplay().asWidget());

      workspaceUsersPresenter.bind();
      display.setWorkspaceUsersView(workspaceUsersPresenter.getDisplay().asWidget());

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
