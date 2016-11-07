package org.zanata.webtrans.client.ui;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.RequestSelectTableRowEvent;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GoToRowLinkLabel extends InlineLabel implements GoToRowLink {
    private final EventBus eventBus;

    @Inject
    public GoToRowLinkLabel(EventBus eventBus, WebTransMessages messages) {
        super();
        this.eventBus = eventBus;
        setTitle(messages.goToThisEntry());
    }

    @Override
    public void prepare(String linkText, final DocumentInfo docInfo,
            final TransUnitId transUnitId) {
        setText(linkText);
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new RequestSelectTableRowEvent(docInfo,
                        transUnitId));
            }
        });
    }

    @Override
    public void setLinkStyle(String styleName) {
        setStyleName(styleName + " icon-back-alt");
    }

    @Override
    public void setDisabledStyle(String styleName) {
        // no op
    }

    @Override
    public Widget asWidget() {
        return this;
    }
}
