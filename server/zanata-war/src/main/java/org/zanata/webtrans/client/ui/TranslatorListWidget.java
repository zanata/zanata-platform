package org.zanata.webtrans.client.ui;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class TranslatorListWidget extends Composite {
    private static TranslatorListWidgetUiBinder ourUiBinder = GWT
            .create(TranslatorListWidgetUiBinder.class);
    @UiField
    Styles style;
    @UiField
    HTMLPanel container;

    private boolean isEmpty = true;

    public TranslatorListWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    public void addTranslator(String name, String color) {
        HTMLPanel liElement = new HTMLPanel("li", "");

        InlineLabel nameLabel = new InlineLabel(name);
        nameLabel.setTitle(name);
        nameLabel.setStyleName(style.userLabel() + " badge");
        nameLabel.getElement().getStyle().setProperty("backgroundColor", color);

        liElement.add(nameLabel);
        container.add(liElement);
        isEmpty = false;
    }

    public void clearTranslatorList() {
        container.clear();
        isEmpty = true;
    }

    public void removeTranslator(String name, String color) {

        for (int i = 0; i < container.getWidgetCount(); i++) {
            HTMLPanel liElement = (HTMLPanel) container.getWidget(i);
            InlineLabel translatorLabel = (InlineLabel) liElement.getWidget(0);

            if (translatorLabel.getText().equals(name)
                    && removeFormat(
                            translatorLabel.getElement().getStyle()
                                    .getProperty("backgroundColor")).equals(
                            removeFormat(color))) {
                container.remove(i);
            }
        }

        isEmpty = container.getWidgetCount() == 0;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * Color string return from userSessionService rgb(xx,xx,xx), Color string
     * return from browser is formatted rgb(xx, xx, xx). Method needed to
     * unformat all color
     *
     * @param color
     *            color
     */
    private String removeFormat(String color) {
        return color.replace(" ", "");
    }

    interface TranslatorListWidgetUiBinder extends
            UiBinder<Widget, TranslatorListWidget> {
    }

    interface Styles extends CssResource {

        String userLabel();
    }
}
