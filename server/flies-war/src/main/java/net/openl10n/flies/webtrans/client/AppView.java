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
package net.openl10n.flies.webtrans.client;

import net.openl10n.flies.webtrans.shared.model.DocumentInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppView extends Composite implements AppPresenter.Display
{

   interface AppViewUiBinder extends UiBinder<LayoutPanel, AppView>
   {
   }

   private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);

   @UiField
   Anchor signOutLink, leaveLink, helpLink, documentsLink;

   @UiField
   SpanElement user, selectedDocumentSpan, selectedDocumentPathSpan;

   @UiField
   LayoutPanel container;

   @UiField(provided = true)
   final Resources resources;

   private Widget documentListView;

   private Widget translationView;

   final WebTransMessages messages;

   @Inject
   public AppView(Resources resources, WebTransMessages messages)
   {
      this.resources = resources;
      this.messages = messages;

      StyleInjector.inject(resources.style().getText(), true);

      initWidget(uiBinder.createAndBindUi(this));

      helpLink.setHref(messages.hrefHelpLink());
      helpLink.setTarget("_BLANK");
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void startProcessing()
   {
   }

   @Override
   public void stopProcessing()
   {
   }

   @Override
   public void showInMainView(MainView view)
   {
      switch (view)
      {
      case Documents:
         container.setWidgetTopBottom(documentListView, 0, Unit.PX, 0, Unit.PX);
         container.setWidgetTopHeight(translationView, 0, Unit.PX, 0, Unit.PX);
         break;
      case Editor:
         container.setWidgetTopBottom(translationView, 0, Unit.PX, 0, Unit.PX);
         container.setWidgetTopHeight(documentListView, 0, Unit.PX, 0, Unit.PX);
         break;
      }
   }

   @Override
   public void setDocumentListView(Widget documentListView)
   {
      this.container.add(documentListView);
      this.documentListView = documentListView;
   }

   @Override
   public void setTranslationView(Widget editorView)
   {
      this.container.add(editorView);
      this.translationView = editorView;
   }


   @Override
   public HasClickHandlers getHelpLink()
   {
      return helpLink;
   }

   @Override
   public HasClickHandlers getLeaveWorkspaceLink()
   {
      return leaveLink;
   }

   @Override
   public HasClickHandlers getSignOutLink()
   {
      return signOutLink;
   }

   @Override
   public HasClickHandlers getDocumentsLink()
   {
      return documentsLink;
   }

   @Override
   public void setUserLabel(String userLabel)
   {
      user.setInnerText(userLabel);
   }

   @Override
   public void setWorkspaceNameLabel(String workspaceNameLabel)
   {
      documentsLink.setText(workspaceNameLabel);
   }

   @Override
   public void setSelectedDocument(DocumentInfo document)
   {
      String path = document.getPath() == null || document.getPath().isEmpty() ? "" : document.getPath() + "/";
      selectedDocumentPathSpan.setInnerText(path);
      selectedDocumentSpan.setInnerText(document.getName());
   }
}
