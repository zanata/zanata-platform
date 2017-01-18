package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.presenter.MainView;

import com.google.gwt.event.shared.GwtEvent;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class ReloadUserConfigUIEvent extends
        GwtEvent<ReloadUserConfigUIHandler> {
    public static final Type<ReloadUserConfigUIHandler> TYPE =
            new Type<>();

    private final MainView view;

    public ReloadUserConfigUIEvent(MainView view) {
        this.view = view;
    }

    public MainView getView() {
        return view;
    }

    @Override
    protected void dispatch(ReloadUserConfigUIHandler handler) {
        handler.onReloadUserConfigUI(this);
    }

    @Override
    public Type<ReloadUserConfigUIHandler> getAssociatedType() {
        return TYPE;
    }

}
