/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.EnterWorkspaceEventHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.events.ShowSideMenuEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData.MESSAGE_TYPE;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.inject.Inject;

/**
 * @author aeng
 * 
 */
public class SideMenuPresenter extends WidgetPresenter<SideMenuPresenter.Display>
{

   private final EditorOptionsPresenter editorOptionsPresenter;
   private final ValidationOptionsPresenter validationOptionsPresenter;
   private final WorkspaceUsersPresenter workspaceUsersPresenter;

   private final UserWorkspaceContext userWorkspaceContext;
   private final WebTransMessages messages;

   private final DispatchAsync dispatcher;

   private boolean isExpended = false;

   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getEditorOptionsButton();

      HasClickHandlers getValidationOptionsButton();

      HasClickHandlers getChatButton();

      void setSelectedTab(Tab tab);

      HasVisibility getEditorOptionsTab();

      HasVisibility getValidationOptionsTab();

      HasVisibility getChatTab();

      HasVisibility getContainer();

      void setParticipantsTitle(int size);

      void setChatTabAlert(boolean alert);

      Tab getCurrentTab();
   }

   public static enum Tab
   {
      EDITOR_OPTION, VALIDATION_OPTION, CHAT, NONE,
   }

   @Inject
   public SideMenuPresenter(Display display, EventBus eventBus, final CachingDispatchAsync dispatcher, final EditorOptionsPresenter editorOptionsPresenter, final ValidationOptionsPresenter validationOptionsPresenter, final WorkspaceUsersPresenter workspaceUsersPresenter, final UserWorkspaceContext userWorkspaceContext, final WebTransMessages messages)
   {
      super(display, eventBus);
      this.editorOptionsPresenter = editorOptionsPresenter;
      this.validationOptionsPresenter = validationOptionsPresenter;
      this.workspaceUsersPresenter = workspaceUsersPresenter;

      this.userWorkspaceContext = userWorkspaceContext;
      this.messages = messages;
      this.dispatcher = dispatcher;
   }

   private void expendSideMenu(boolean isExpend)
   {
      isExpended = isExpend;
      eventBus.fireEvent(new ShowSideMenuEvent(isExpended));
      if(!isExpended)
      {
         display.setSelectedTab(Tab.NONE);
      }
   }

   @Override
   protected void onBind()
   {
      editorOptionsPresenter.bind();
      validationOptionsPresenter.bind();
      workspaceUsersPresenter.bind();

      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), new WorkspaceContextUpdateEventHandler()
      {
         @Override
         public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
         {
            userWorkspaceContext.setProjectActive(event.isProjectActive());

            display.getContainer().setVisible(!userWorkspaceContext.hasReadOnlyAccess());
            display.getChatTab().setVisible(!userWorkspaceContext.hasReadOnlyAccess());
            display.getEditorOptionsTab().setVisible(!userWorkspaceContext.hasReadOnlyAccess());
            display.getValidationOptionsTab().setVisible(!userWorkspaceContext.hasReadOnlyAccess());
         }
      }));

      display.getEditorOptionsButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (!userWorkspaceContext.hasReadOnlyAccess())
            {
               if (!isExpended)
               {
                  expendSideMenu(true);
                  display.setSelectedTab(Tab.EDITOR_OPTION);
               }
               else if (display.getCurrentTab() != Tab.EDITOR_OPTION)
               {
                  display.setSelectedTab(Tab.EDITOR_OPTION);
               }
               else
               {
                  expendSideMenu(false);
               }
            }
         }
      });

      display.getValidationOptionsButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (!userWorkspaceContext.hasReadOnlyAccess())
            {
               if (!isExpended)
               {
                  expendSideMenu(true);
                  display.setSelectedTab(Tab.VALIDATION_OPTION);
               }
               else if (display.getCurrentTab() != Tab.VALIDATION_OPTION)
               {
                  display.setSelectedTab(Tab.VALIDATION_OPTION);
               }
               else
               {
                  expendSideMenu(false);
               }
            }
         }
      });

      display.getChatButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (!userWorkspaceContext.hasReadOnlyAccess())
            {
               if (!isExpended)
               {
                  expendSideMenu(true);
                  display.setSelectedTab(Tab.CHAT);
               }
               else if (display.getCurrentTab() != Tab.CHAT)
               {
                  display.setSelectedTab(Tab.CHAT);
               }
               else
               {
                  expendSideMenu(false);
               }
            }
         }
      });

      // We won't receive the EnterWorkspaceEvent generated by our own login,
      // because this presenter is not bound until we get the callback from
      // EventProcessor.
      // Thus we load the translator list here.
      loadTranslatorList();
   }

   private void loadTranslatorList()
   {
      dispatcher.execute(new GetTranslatorList(), new AsyncCallback<GetTranslatorListResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("error fetching translators list: " + caught.getMessage());
         }

         @Override
         public void onSuccess(GetTranslatorListResult result)
         {
            workspaceUsersPresenter.initUserList(result.getTranslatorList());
            display.setParticipantsTitle(result.getSize());
         }
      });

      registerHandler(eventBus.addHandler(ExitWorkspaceEvent.getType(), new ExitWorkspaceEventHandler()
      {
         @Override
         public void onExitWorkspace(ExitWorkspaceEvent event)
         {
            workspaceUsersPresenter.removeTranslator(event.getEditorClientId(), event.getPerson());
            display.setParticipantsTitle(workspaceUsersPresenter.getTranslatorsSize());
         }
      }));

      registerHandler(eventBus.addHandler(EnterWorkspaceEvent.getType(), new EnterWorkspaceEventHandler()
      {
         @Override
         public void onEnterWorkspace(EnterWorkspaceEvent event)
         {
            workspaceUsersPresenter.addTranslator(event.getEditorClientId(), event.getPerson(), null);
            workspaceUsersPresenter.dispatchChatAction(null, messages.hasJoinedWorkspace(event.getPerson().getId().toString()), MESSAGE_TYPE.SYSTEM_MSG);
            display.setParticipantsTitle(workspaceUsersPresenter.getTranslatorsSize());
         }
      }));

      registerHandler(eventBus.addHandler(PublishWorkspaceChatEvent.getType(), new PublishWorkspaceChatEventHandler()
      {
         @Override
         public void onPublishWorkspaceChat(PublishWorkspaceChatEvent event)
         {
            if (display.getCurrentTab() != Tab.CHAT)
            {
               display.setChatTabAlert(true);
            }
         }
      }));
   }

   @Override
   protected void onUnbind()
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected void onRevealDisplay()
   {
      // TODO Auto-generated method stub

   }

   public void showEditorMenu(boolean showEditorMenu)
   {
      display.getEditorOptionsTab().setVisible(showEditorMenu);
      display.getValidationOptionsTab().setVisible(showEditorMenu);

      if (showEditorMenu && isExpended)
      {
         display.setSelectedTab(Tab.CHAT);
      }
      else
      {
         display.setSelectedTab(Tab.NONE);
      }
   }
}
