package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SideMenuView extends Composite implements SideMenuDisplay {
    private static SideMenuViewUiBinder uiBinder = GWT
            .create(SideMenuViewUiBinder.class);
    private Listener listener;

    interface SideMenuViewUiBinder extends UiBinder<Widget, SideMenuView> {
    }

    @UiField
    Anchor notificationTab, optionsTab, validationOptionsTab, chatTab;

    @UiField
    InlineLabel notificationLabel;

    @UiField
    TabLayoutPanel container;

    @Inject
    public SideMenuView(final WebTransMessages messages,
            final OptionsDisplay optionView,
            final ValidationOptionsDisplay validationOptionView,
            final WorkspaceUsersDisplay workspaceUsersView,
            final NotificationDisplay notificationView) {
        initWidget(uiBinder.createAndBindUi(this));

        notificationTab.setTitle(messages.notification());
        optionsTab.setTitle(messages.options());
        validationOptionsTab.setTitle(messages.validationOptions());
        chatTab.setTitle(messages.chatRoom());

        container.add(notificationView.asWidget());
        container.add(workspaceUsersView.asWidget());
        container.add(optionView.asWidget());
        container.add(validationOptionView.asWidget());
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @UiHandler("optionsTab")
    public void onOptionsClick(ClickEvent event) {
        listener.onOptionsClick();
    }

    @UiHandler("notificationTab")
    public void onNotificationClick(ClickEvent event) {
        listener.onNotificationClick();
    }

    @UiHandler("validationOptionsTab")
    public void onValidationOptionsClick(ClickEvent event) {
        listener.onValidationOptionsClick();
    }

    @UiHandler("chatTab")
    public void onChatClick(ClickEvent event) {
        listener.onChatClick();
    }

    @Override
    public void setSelectedTab(int view) {
        setDefaultTabStyle(optionsTab);
        setDefaultTabStyle(validationOptionsTab);
        setDefaultTabStyle(chatTab);
        setDefaultTabStyle(notificationTab);

        switch (view) {
        case OPTION_VIEW:
            container.selectTab(OPTION_VIEW);
            setSelectTabStyle(optionsTab);
            break;
        case VALIDATION_OPTION_VIEW:
            container.selectTab(VALIDATION_OPTION_VIEW);
            setSelectTabStyle(validationOptionsTab);
            break;
        case WORKSPACEUSER_VIEW:
            container.selectTab(WORKSPACEUSER_VIEW);
            setSelectTabStyle(chatTab);
            setChatTabAlert(false);
            break;
        case NOTIFICATION_VIEW:
            container.selectTab(NOTIFICATION_VIEW);
            setSelectTabStyle(notificationTab);
            break;
        default:
            break;
        }
    }

    private void setDefaultTabStyle(UIObject tab) {
        tab.removeStyleName("bg--low");
        tab.addStyleName("bg--lowest");
    }

    private void setSelectTabStyle(UIObject tab) {
        tab.addStyleName("bg--low");
        tab.removeStyleName("bg--lowest");
    }

    @Override
    public void setChatTabAlert(boolean alert) {
        if (alert) {
            chatTab.addStyleName("txt--unsure");
        } else {
            chatTab.removeStyleName("txt--unsure");
        }
    }

    @Override
    public int getCurrentTab() {
        return container.getSelectedIndex();
    }

    @Override
    public void setNotificationText(int count, Severity severity) {
        notificationLabel.setText(String.valueOf(count));
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setChatTabVisible(boolean visible) {
        chatTab.setVisible(visible);
    }

    @Override
    public void setValidationOptionsTabVisible(boolean visible) {
        validationOptionsTab.setVisible(visible);
    }

    @Override
    public void setOptionsTabVisible(boolean visible) {
        optionsTab.setVisible(visible);
    }
}
