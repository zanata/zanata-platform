package org.zanata.webtrans.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;

public class ReferencePanel extends Composite {
    private static ReferencePanelUiBinder uiBinder = GWT
            .create(ReferencePanelUiBinder.class);
    @UiField
    Label referenceLabel;
    @UiField
    DisclosurePanel disclosurePanel;
    @UiField
    Styles style;

    public ReferencePanel() {
        initWidget(uiBinder.createAndBindUi(this));
        //this is to remove the .header class so that it won't get style from menu.css
        disclosurePanel.getHeader().getParent().setStyleName(style.header());
    }

    public void setReferenceText(String text) {
        referenceLabel.setText(text);
    }

    interface ReferencePanelUiBinder extends UiBinder<DisclosurePanel,
            ReferencePanel> {
    }

    interface Styles extends CssResource {
        String container();

        String header();

        String referenceLabel();
    }
}
