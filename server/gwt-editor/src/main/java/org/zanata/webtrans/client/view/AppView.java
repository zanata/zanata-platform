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
package org.zanata.webtrans.client.view;

import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.util.CoverageIgnore;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.presenter.NotificationDetailListener;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.Breadcrumb;
import org.zanata.webtrans.client.ui.HasTranslationStats.LabelFormat;
import org.zanata.webtrans.client.ui.NotificationDetailsBox;
import org.zanata.webtrans.client.ui.TransUnitCountBar;
import org.zanata.webtrans.client.ui.UnorderedListWidget;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AppView extends Composite implements AppDisplay,
        NotificationDetailListener {

    private Timer deactivateNotificationTimer = new Timer() {

        @Override
        public void run() {
            deactivateNotification(notificationList);
        }
    };

    interface AppViewUiBinder extends UiBinder<LayoutPanel, AppView> {
    }

    interface Styles extends CssResource {
        String disableTab();

        String selectedTab();

        String highlightedTab();
    }

    private static AppViewUiBinder uiBinder = GWT.create(AppViewUiBinder.class);
    private static NotificationTemplate notificationTemplate = GWT
            .create(NotificationTemplate.class);

    @UiField(provided = true)
    TransUnitCountBar translationStatsBar;

    @UiField
    InlineLabel readOnlyLabel;

    @UiField
    InlineLabel obsoleteLabel;

    @UiField(provided = true)
    Breadcrumb selectedDocumentSpan;

    @UiField(provided = true)
    Breadcrumb projectLink;

    @UiField(provided = true)
    Breadcrumb versionLink;

    @UiField(provided = true)
    Breadcrumb filesLink;

    @UiField
    LayoutPanel sideMenuContainer, rootContainer;

    @UiField
    HTMLPanel contentContainer;

    @UiField
    TabLayoutPanel content;

    @UiField
    Styles style;

    @UiField
    Anchor editorTab, searchAndReplaceTab, documentListTab, keyShortcuts;

    @UiField(provided = true)
    Anchor newEditorLink;

    @UiField
    UnorderedListWidget notifications;
    @UiField
    UListElement notificationList;

    private final NotificationDetailsBox notificationDetailsBox;

    private final WebTransMessages messages;

    private Listener listener;

    private final UserWorkspaceContext userWorkspaceContext;

    @Inject
    public AppView(WebTransMessages messages,
            DocumentListDisplay documentListView,
            SearchResultsPresenter.Display searchResultsView,
            TranslationPresenter.Display translationView,
            SideMenuDisplay sideMenuView,
            KeyShortcutPresenter keyShortcutPresenter,
            final UserWorkspaceContext userWorkspaceContext) {
        // this must be initialized before uiBinder.createAndBindUi(), or an
        // exception will be thrown at runtime
        this.userWorkspaceContext = userWorkspaceContext;
        this.messages = messages;
        translationStatsBar =
                new TransUnitCountBar(userWorkspaceContext, messages,
                        LabelFormat.PERCENT_COMPLETE_HRS, true);
        translationStatsBar.setVisible(false); // hide until there is a value to

        projectLink =
                new Breadcrumb(true, false,
                        Application.getProjectHomeURL(userWorkspaceContext
                                .getWorkspaceContext().getWorkspaceId()));
        versionLink =
                new Breadcrumb(false, false,
                        Application.getVersionHomeURL(userWorkspaceContext
                                .getWorkspaceContext().getWorkspaceId()));
        filesLink = new Breadcrumb(false, false, "");
        // filesLink.setHref(Application.getVersionFilesURL(userWorkspaceContext.getWorkspaceContext().getWorkspaceId()));
        selectedDocumentSpan = new Breadcrumb(false, true, "");

        newEditorLink = new Anchor();
        newEditorLink.setTarget("_blank");
        newEditorLink.setText(messages.newEditorMessage());
        newEditorLink.addStyleName("is-invisible");

        initWidget(uiBinder.createAndBindUi(this));

        readOnlyLabel.setTitle(messages.readOnlyTooltip());
        obsoleteLabel.setTitle(messages.obsoleteTooltip());

        keyShortcuts.setTitle(messages.availableKeyShortcutsTitle());

        sideMenuContainer.add(sideMenuView.asWidget());

        searchAndReplaceTab.setTitle(messages.projectWideSearchAndReplace());
        documentListTab.setTitle(messages.documentListTitle());
        editorTab.setTitle(messages.editor());

        content.add(documentListView.asWidget());
        content.add(translationView.asWidget());
        content.add(searchResultsView.asWidget());

        notificationDetailsBox =
                new NotificationDetailsBox(messages, keyShortcutPresenter);

        notifications.ensureDebugId("notifications");

        Window.enableScrolling(false);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    // Order of the tab
    private final static int DOCUMENT_VIEW = 0;
    private final static int EDITOR_VIEW = 1;
    private final static int SEARCH_AND_REPLACE_VIEW = 2;
    private final static int REVIEW_VIEW = 3;

    @Override
    public void showInMainView(MainView view) {
        switch (view) {
        case Documents:
            content.selectTab(DOCUMENT_VIEW);
            selectedDocumentSpan.setVisible(false);
            newEditorLink.addStyleName("is-invisible");
            setSelectedTab(documentListTab);
            break;
        case Search:
            content.selectTab(SEARCH_AND_REPLACE_VIEW);
            selectedDocumentSpan.setVisible(true);
            newEditorLink.addStyleName("is-invisible");
            setSelectedTab(searchAndReplaceTab);
            break;
        case Editor:
            content.selectTab(EDITOR_VIEW);
            selectedDocumentSpan.setVisible(true);
            newEditorLink.removeStyleName("is-invisible");
            setSelectedTab(editorTab);
        }
    }

    private void setSelectedTab(Widget tab) {
        editorTab.removeStyleName(style.selectedTab());
        searchAndReplaceTab.removeStyleName(style.selectedTab());
        documentListTab.removeStyleName(style.selectedTab());

        tab.addStyleName(style.selectedTab());
    }

    @Override
    public void setProjectLinkLabel(String text) {
        projectLink.setText(text);
    }

    @Override
    public void setVersionLinkLabel(String text) {
        versionLink.setText(text);
    }

    @Override
    public void setFilesLinkLabel(String text) {
        filesLink.setText(text);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setDocumentLabel(String docPath, String docName) {
        String selectedDocId = docPath + docName;
        selectedDocumentSpan.setText(selectedDocId);

        newEditorLink.setHref(Application.getNewEditorLink(
                userWorkspaceContext.getWorkspaceContext().getWorkspaceId(),
                selectedDocId.replace("/", ",")));
    }

    @Override
    public void setStats(ContainerTranslationStatistics transStats,
            boolean statsByWords) {
        translationStatsBar.setStats(transStats, statsByWords);
        translationStatsBar.setVisible(true);
    }

    @Override
    public void setReadOnlyVisible(boolean visible) {
        readOnlyLabel.setVisible(visible);
    }

    @Override
    public void setObsoleteVisible(boolean visible) {
        obsoleteLabel.setVisible(visible);
    }

    private final static double MIN_MENU_WIDTH = 2;
    private final static double EXPENDED_MENU_RIGHT = 23;

    private final static double MINIMISED_EDITOR_RIGHT = 21.5;
    private final static int ANIMATE_DURATION = 100;

    @Override
    public void showSideMenu(boolean isShowing) {
        rootContainer.forceLayout();
        if (isShowing) {
            rootContainer.setWidgetLeftRight(contentContainer, 0.0, Unit.EM,
                    MINIMISED_EDITOR_RIGHT, Unit.EM);
            rootContainer.setWidgetRightWidth(sideMenuContainer, 0.0, Unit.PX,
                    EXPENDED_MENU_RIGHT, Unit.EM);
        } else {
            rootContainer.setWidgetLeftRight(contentContainer, 0.0, Unit.EM,
                    0.0, Unit.EM);
            rootContainer.setWidgetRightWidth(sideMenuContainer, 0.0, Unit.EM,
                    MIN_MENU_WIDTH, Unit.EM);
        }
        rootContainer.animate(ANIMATE_DURATION);
    }

    @UiHandler("keyShortcuts")
    public void onKeyShortcutsIconClick(ClickEvent event) {
        listener.onKeyShortcutsClicked();
    }

    @Override
    public void enableTab(MainView view, boolean enable) {
        switch (view) {
        case Search:
            enableTab(searchAndReplaceTab, enable);
            break;
        case Documents:
            enableTab(documentListTab, enable);
            break;
        case Editor:
            enableTab(editorTab, enable);
            break;
        }
    }

    @UiHandler("filesLink")
    public void onFilesLinkClick(ClickEvent event) {
        listener.onDocumentListClicked();
    }

    @UiHandler("documentListTab")
    public void onDocumentListTabClick(ClickEvent event) {
        listener.onDocumentListClicked();
    }

    @UiHandler("editorTab")
    public void onEditorTabClick(ClickEvent event) {
        listener.onEditorClicked();
    }

    @UiHandler("searchAndReplaceTab")
    public void onSearchAndReplaceTabTabClick(ClickEvent event) {
        listener.onSearchAndReplaceClicked();
    }

    private void enableTab(Widget tab, boolean enable) {
        if (enable) {
            tab.removeStyleName(style.disableTab());
        } else {
            tab.addStyleName(style.disableTab());
        }
    }

    @Override
    public void setKeyboardShorcutColor(boolean aliasKeyListening) {
        if (aliasKeyListening) {
            keyShortcuts.addStyleName(style.highlightedTab());
        } else {
            keyShortcuts.removeStyleName(style.highlightedTab());
        }
    }

    public void showNotification(NotificationEvent notification) {
        notificationList.setInnerHTML(
                createListItem(getMessageClass(notification.getSeverity()),
                        notification.getMessage()).asString());
        activateNotification(notificationList);
        if (notification.getSeverity() == NotificationEvent.Severity.Info || notification.getSeverity() ==
                NotificationEvent.Severity.Warning) {
            deactivateNotificationTimer.schedule(5000);
        }
    }

    @Override
    public void showNotificationDetail(NotificationEvent notificationEvent) {
        notificationDetailsBox.setMessage(notificationEvent);

        notificationDetailsBox.center();
    }

    @Override
    public void closeMessage(NotificationEvent notificationEvent) {
        notifications.clear();
    }

    private static SafeHtml createListItem(String messageStyle, String message) {
        return notificationTemplate.listItem(messageStyle,
                new SafeHtmlBuilder().appendEscaped(message).toSafeHtml());
    }

    private static String getMessageClass(NotificationEvent.Severity severity) {
        switch (severity) {
            case Warning:
                return "message--warning";
            case Error:
                return "message--danger app-error";
            case Info:
                return "message--highlight";
        }
        return "message--highlight";
    }

    // @formatter:off
    @CoverageIgnore("JSNI")
    private static native void activateNotification(Element element)/*-{
      $wnd.zanata.messages.activate(element);
    }-*/;
    @CoverageIgnore("JSNI")
    private static native void deactivateNotification(Element element)/*-{
      $wnd.zanata.messages.deactivate(element);
    }-*/;
    // @formatter:on

    public interface NotificationTemplate extends SafeHtmlTemplates {
        @Template("<li class='{0} message--removable js-message-removable'>{1}" +
                "<a href='#' class='message__remove js-message-remove'><i class='i i--cancel'></i></a>" +
                "</li>")
        SafeHtml listItem(String messageStyle, SafeHtml message);
    }
}
