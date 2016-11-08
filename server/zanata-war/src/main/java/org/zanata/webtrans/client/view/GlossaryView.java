package org.zanata.webtrans.client.view;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.Anchor;

import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.EnumListBox;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.client.ui.SearchTypeRenderer;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.zanata.webtrans.shared.rpc.LuceneQuery;

public class GlossaryView extends Composite implements GlossaryDisplay {
    private static GlossaryViewUiBinder uiBinder = GWT
            .create(GlossaryViewUiBinder.class);

    interface GlossaryViewUiBinder extends UiBinder<Widget, GlossaryView> {
    }

    @UiField
    TextBox glossaryTextBox;

    @UiField
    Button searchButton, clearButton;

    @UiField(provided = true)
    ValueListBox<SearchType> searchType;

    @UiField
    HTMLPanel data;

    @UiField
    UiMessages messages;

    private final FlexTable resultTable;

    private final Label loadingLabel, noResultFoundLabel;

    private GlossaryDisplay.Listener listener;

    private final static int TYPE_COL = 0;
    private final static int SOURCE_COL = 1;
    private final static int TARGET_COL = 2;
    private final static int ACTION_COL = 3;
    private final static int DETAILS_COL = 4;

    @Inject
    public GlossaryView(SearchTypeRenderer searchTypeRenderer) {
        searchType =
            new EnumListBox<SearchType>(SearchType.class,
                searchTypeRenderer);

        initWidget(uiBinder.createAndBindUi(this));

        resultTable = new FlexTable();
        resultTable.setStyleName("resultTable");
        resultTable.setCellSpacing(0);
        resultTable.setCellPadding(3);

        FlexCellFormatter formatter = resultTable.getFlexCellFormatter();
        formatter.setStyleName(0, TYPE_COL, "zeta w--1-12");
        formatter.setStyleName(0, SOURCE_COL, "zeta");
        formatter.setStyleName(0, TARGET_COL, "zeta");
        formatter.setStyleName(0, ACTION_COL, "zeta txt--align-center smallCol");
        formatter.setStyleName(0, DETAILS_COL, "zeta txt--align-center smallCol");

        resultTable
                .setWidget(0, SOURCE_COL, new Label(messages.srcTermLabel()));
        resultTable.setWidget(0, TARGET_COL,
                new Label(messages.targetTermLabel()));
        resultTable.setWidget(0, ACTION_COL, null);
        resultTable.setWidget(0, DETAILS_COL,
                new Label(messages.detailsLabel()));

        loadingLabel = new Label(messages.searching());
        loadingLabel.setStyleName("gamma");
        noResultFoundLabel = new Label(messages.foundNoGlossaryResults());
        noResultFoundLabel.setStyleName("gamma");

        data.add(loadingLabel);

        // debug id
        glossaryTextBox.ensureDebugId("glossaryTextBox");
        searchType.ensureDebugId("glossarySearchType");
        searchButton.ensureDebugId("glossarySearchButton");
        clearButton.ensureDebugId("glossaryClearButton");
        resultTable.ensureDebugId("glossaryResultTable");
        noResultFoundLabel.ensureDebugId("glossaryNoResult");

        glossaryTextBox.setMaxLength(LuceneQuery.QUERY_MAX_LENGTH);
    }

    @UiHandler("glossaryTextBox")
    void onGlossaryTextBoxKeyPress(KeyPressEvent event) {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
            searchButton.click();
        }
    }

    @UiHandler("clearButton")
    public void onClearButtonClick(ClickEvent event) {
        listener.clearContent();
    }

    @UiHandler("searchButton")
    public void onSearchButtonClick(ClickEvent event) {
        listener.fireSearchEvent();
    }

    public TextBox getGlossaryTextBox() {
        return glossaryTextBox;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void startProcessing() {
        data.clear();
        data.add(loadingLabel);

        clearTableContent();
    }

    @Override
    public void stopProcessing(boolean showResult) {
        data.clear();
        if (!showResult) {
            data.add(noResultFoundLabel);
        } else {
            data.add(resultTable);
        }
    }

    @Override
    public void clearTableContent() {
        while (resultTable.getRowCount() > 1) {
            resultTable.removeRow(resultTable.getRowCount() - 1);
        }
    }

    @Override
    public HasValue<SearchType> getSearchType() {
        return searchType;
    }

    @UiHandler("glossaryTextBox")
    public void onGlossaryTextBoxFocus(FocusEvent event) {
        listener.onFocus(true);
    }

    @UiHandler("glossaryTextBox")
    public void onGlossaryTextBoxBlur(BlurEvent event) {
        listener.onFocus(false);
    }

    @Override
    public void renderTable(ArrayList<GlossaryResultItem> glossaries) {
        for (int i = 0; i < glossaries.size(); i++) {
            final GlossaryResultItem item = glossaries.get(i);
            FlexCellFormatter cellFormatter = resultTable.getFlexCellFormatter();

            resultTable.setWidget(i + 1, TYPE_COL,
                    getTypePanel(item.getQualifiedName()));
            cellFormatter.setStyleName(i + 1, TYPE_COL, "txt--align-center");
            cellFormatter.setVerticalAlignment(i + 1,
                    TYPE_COL, HasVerticalAlignment.ALIGN_MIDDLE);

            resultTable.setWidget(i + 1, SOURCE_COL,
                    new HighlightingLabel(item.getSource()));
            resultTable.setWidget(i + 1, TARGET_COL,
                    new HighlightingLabel(item.getTarget()));

            Button copyButton = new Button(messages.copy());
            copyButton.setTitle(messages.copyTooltip());
            copyButton.addStyleName("button--small");
            copyButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    listener.fireCopyEvent(item);
                }
            });

            resultTable.setWidget(i + 1, ACTION_COL, copyButton);
            cellFormatter.setStyleName(i + 1, ACTION_COL,
                    "txt--align-center smallCol");
            cellFormatter.setVerticalAlignment(i + 1,
                ACTION_COL, HasVerticalAlignment.ALIGN_MIDDLE);

            Anchor infoCell = new Anchor();
            infoCell.setStyleName("i i--info txt--lead");
            infoCell.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    listener.showGlossaryDetail(item);
                }
            });

            resultTable.setWidget(i + 1, DETAILS_COL, infoCell);
            cellFormatter.setStyleName(i + 1, DETAILS_COL,
                    "txt--align-center smallCol");
            cellFormatter.setVerticalAlignment(i + 1,
                    DETAILS_COL, HasVerticalAlignment.ALIGN_MIDDLE);
        }
    }

    private HTMLPanel getTypePanel(String qualifiedName) {
        String typeHTML = "<i class='i "
            + getTypeIconClass(qualifiedName) + "' />";
        HTMLPanel panel = new HTMLPanel(typeHTML);
        panel.setTitle(getTypeTitle(qualifiedName));
        return panel;
    }

    private String getTypeIconClass(String qualifiedName) {
        if(Application.isProjectGlossary(qualifiedName)) {
            return "i--project";
        }
        return "i--translate";
    }

    private String getTypeTitle(String qualifiedName) {
        if(Application.isProjectGlossary(qualifiedName)) {
            return messages.glossaryProjectTypeTitle();
        }
        return messages.glossaryGlobalTypeTitle();
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
