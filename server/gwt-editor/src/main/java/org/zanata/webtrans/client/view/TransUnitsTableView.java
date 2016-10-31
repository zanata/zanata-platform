package org.zanata.webtrans.client.view;

import java.util.List;

import org.zanata.util.CoverageIgnore;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.FilterViewConfirmationDisplay;
import org.zanata.webtrans.client.ui.LoadingPanel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TransUnitsTableView extends Composite implements
        TransUnitsTableDisplay {
    private static TransUnitEditViewUiBinder uiBinder = GWT
            .create(TransUnitEditViewUiBinder.class);

    @UiField
    Grid transUnitTable;

    @UiField
    WebTransMessages messages;

    @UiField
    ScrollPanel root;

    private final FilterViewConfirmationDisplay filterViewConfirmationDisplay;
    private final LoadingPanel loadingPanel;
    private final Label noContentLabel = new Label();
    private Listener listener;

    // below timer is a hacky fix for firefox (on first load codemirror instance
    // won't show correctly and needs refresh
    // we only need to do this once on first load (WEIRD!!)
    private static boolean firstTimeLoading = true;
    private Timer timer = new Timer() {

        @Override
        public void run() {
            listener.refreshView();
        }
    };

    @Inject
    public TransUnitsTableView(
            FilterViewConfirmationDisplay filterViewConfirmationDisplay,
            LoadingPanel loadingPanel) {
        this.filterViewConfirmationDisplay = filterViewConfirmationDisplay;
        this.loadingPanel = loadingPanel;
        initWidget(uiBinder.createAndBindUi(this));
        noContentLabel.setText(messages.noContent());
        noContentLabel.setStyleName("gamma");

        transUnitTable.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HTMLTable.Cell cellForEvent =
                        transUnitTable.getCellForEvent(event);
                if (cellForEvent != null) {
                    selectRow(cellForEvent.getRowIndex());
                }
            }
        });
        transUnitTable.resize(0, 2);
        transUnitTable.getColumnFormatter().setWidth(0, "49%");
        transUnitTable.getColumnFormatter().setWidth(1, "51%");
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                listener.refreshView();
            }
        });

        transUnitTable.ensureDebugId("transUnitTable");
    }

    @Override
    public void setThemes(String theme) {
        transUnitTable.setStyleName("transUnitTable");
        transUnitTable.addStyleName(theme);
    }

    private void selectRow(int rowIndex) {
        listener.onRowSelected(rowIndex);
    }

    @Override
    public void addFilterConfirmationHandler(
            FilterViewConfirmationDisplay.Listener listener) {
        filterViewConfirmationDisplay.setListener(listener);
    }

    @Override
    public void showFilterConfirmation() {
        filterViewConfirmationDisplay.center();
    }

    @Override
    public void hideFilterConfirmation() {
        filterViewConfirmationDisplay.hide();
    }

    @Override
    public void buildTable(List<? extends NeedsRefresh> sourceDisplays,
            List<? extends NeedsRefresh> targetDisplays) {
        showEmptyContentIfNoData(sourceDisplays.size());

        transUnitTable.resizeRows(sourceDisplays.size());
        HTMLTable.CellFormatter cellFormatter =
                transUnitTable.getCellFormatter();

        for (int i = 0; i < sourceDisplays.size(); i++) {
            NeedsRefresh sourceDisplay = sourceDisplays.get(i);
            NeedsRefresh targetDisplay = targetDisplays.get(i);

            transUnitTable.setWidget(i, 0, sourceDisplay);
            transUnitTable.setWidget(i, 1, targetDisplay);

            cellFormatter.setVerticalAlignment(i, 0,
                    HasVerticalAlignment.ALIGN_TOP);
            cellFormatter.setVerticalAlignment(i, 1,
                    HasVerticalAlignment.ALIGN_TOP);
            cellFormatter.addStyleName(i, 0, "transUnitCol");
            cellFormatter.addStyleName(i, 1, "transUnitCol");

            sourceDisplay.refresh();
            targetDisplay.refresh();
        }
    }

    private void showEmptyContentIfNoData(int dataSize) {
        if (dataSize == 0) {
            root.setWidget(noContentLabel);
        } else {
            root.setWidget(transUnitTable);
        }
    }

    @Override
    public void applySelectedStyle(int rowIndex) {
        HTMLTable.RowFormatter rowFormatter = transUnitTable.getRowFormatter();
        for (int i = 0; i < transUnitTable.getRowCount(); i++) {
            if (i == rowIndex) {
                rowFormatter.addStyleName(i, "selected");
            } else {
                rowFormatter.removeStyleName(i, "selected");
            }
        }
    }

    @Override
    public void delayRefresh() {
        timer.schedule(100);
    }

    @Override
    public void ensureVisible(TargetContentsDisplay currentDisplay) {
        Widget transUnitRow = currentDisplay.asWidget();
        scrollToElement(root.getElement(), transUnitRow.getElement());
    }

    @CoverageIgnore("JSNI")
    private native void scrollToElement(Element scroll, Element item) /*-{
      if (!item || !scroll)  {
        return;
      }
      //need time out due to dom loading
      setTimeout(function () {
        var realOffset = 0;
        var loopItem = item;
        while (loopItem && (loopItem != scroll)) {
          realOffset += loopItem.offsetTop;
          loopItem = loopItem.offsetParent;
        }

        // handle scrolling when user clicks
        if((realOffset + item.offsetHeight) > (scroll.scrollTop + scroll.offsetHeight)) {
          //scroll if text area is partial visible on bottom
          scroll.scrollTop = realOffset - scroll.offsetHeight / 2;
        } else if(realOffset < scroll.scrollTop) {
          //scroll if text area is partial visible on top
          scroll.scrollTop = realOffset;
        }
      }, 100);
    }-*/;

    @Override
    public void setRowSelectionListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingPanel.center();
        } else {
            loadingPanel.hide();
            if (firstTimeLoading) {
                timer.schedule(100);
                firstTimeLoading = false;
            }
        }
    }

    interface TransUnitEditViewUiBinder extends
            UiBinder<Widget, TransUnitsTableView> {
    }
}
