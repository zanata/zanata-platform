package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.common.base.Strings;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

public class TransUnitDetailsPanel extends Composite {
    private static TransUnitDetailsPanelUiBinder uiBinder = GWT
            .create(TransUnitDetailsPanelUiBinder.class);
    @UiField
    TableEditorMessages messages;

    @UiField
    Label headerLabel;
    @UiField
    InlineLabel resId, msgContext, refs, flags, sourceComment, lastModifiedBy,
            lastModifiedTime;
    @UiField
    DisclosurePanel disclosurePanel;
    @UiField
    Styles style;
    @UiField
    HTMLPanel lastModified;
    private String metaInfo = "";


    public TransUnitDetailsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        // this is to remove the .header class so that it won't get style from
        // menu.css
        disclosurePanel.getHeader().getParent().setStyleName("l--pad-left-quarter txt--mini");
    }

    public void setDetails(TransUnit transUnit) {
        metaInfo = "";
        resId.setText(transUnit.getResId());

        String context = Strings.nullToEmpty(transUnit.getMsgContext());
        setSourceMetaData(msgContext, context);

        String reference = Strings.nullToEmpty(transUnit.getSourceRefs());
        setSourceMetaData(refs, reference);

        String comment = Strings.nullToEmpty(transUnit.getSourceComment());
        setSourceMetaData(sourceComment, comment);

        flags.setText(Strings.nullToEmpty(transUnit.getSourceFlags()));
        setSourceMetaData(flags, transUnit.getSourceFlags());

        String person = Strings.nullToEmpty(transUnit.getLastModifiedBy());
        lastModifiedBy.setText(person);

        String date =
                transUnit.getLastModifiedTime() != null ? DateUtil
                        .formatShortDate(transUnit
                                .getLastModifiedTime()) : "";

        lastModifiedTime.setText(date);

        if (Strings.isNullOrEmpty(person)
                || Strings.isNullOrEmpty(date)) {
            lastModified.addStyleName("is-hidden");
        }

        metaInfo = append(metaInfo, getHeader("MsgCtx", context));
        metaInfo = append(metaInfo, getHeader("Comment", comment));
        metaInfo = append(metaInfo, getHeader("Refs", reference));

        String transUnitId = "";
        if (!GWT.isProdMode()) {
            transUnitId = "Id: " + transUnit.getId().toString();
        }
        headerLabel.setText(transUnitId
                + messages.transUnitDetailsHeadingWithInfo(
                        transUnit.getRowIndex(), metaInfo));
    }

    private void setSourceMetaData(Label label, String value) {
        if(Strings.isNullOrEmpty(value)) {
            label.addStyleName("label");
            label.setText("No content");
        } else {
            label.setText(value);
        }
    }

    private String append(String str, String valueToAppend) {
        if(Strings.isNullOrEmpty(str)) {
            return valueToAppend;
        } else {
            return str + ", " + valueToAppend;
        }
    }

    private String getHeader(String header, String value) {
        if(Strings.isNullOrEmpty(value)) {
            return "";
        }
        return header + ": " + value;
    }

    public boolean hasNoMetaInfo() {
        return Strings.isNullOrEmpty(metaInfo);
    }

    interface TransUnitDetailsPanelUiBinder extends
            UiBinder<DisclosurePanel, TransUnitDetailsPanel> {
    }

    interface Styles extends CssResource {
        String headerLabel();
    }
}
