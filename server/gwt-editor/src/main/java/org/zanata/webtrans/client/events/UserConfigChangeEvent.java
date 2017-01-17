package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.presenter.MainView;

import com.google.gwt.event.shared.GwtEvent;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class UserConfigChangeEvent extends GwtEvent<UserConfigChangeHandler> {
    public static final Type<UserConfigChangeHandler> TYPE =
            new Type<>();
    public static final UserConfigChangeEvent EDITOR_CONFIG_CHANGE_EVENT =
            new UserConfigChangeEvent(MainView.Editor);
    public static final UserConfigChangeEvent DOCUMENT_CONFIG_CHANGE_EVENT =
            new UserConfigChangeEvent(MainView.Documents);
    public static final UserConfigChangeEvent COMMON_CONFIG_CHANGE_EVENT =
            new UserConfigChangeEvent(null);

    private final MainView view;

    // user constant fields
    private UserConfigChangeEvent(MainView view) {
        this.view = view;
    }

    public MainView getView() {
        return view;
    }

    @Override
    protected void dispatch(UserConfigChangeHandler handler) {
        handler.onUserConfigChanged(this);
    }

    @Override
    public Type<UserConfigChangeHandler> getAssociatedType() {
        return TYPE;
    }

}
