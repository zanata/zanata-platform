package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.GlossaryDetailsDisplay;
import org.zanata.webtrans.client.view.GlossaryDisplay;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class GlossaryDetailsPresenter extends
        WidgetPresenter<GlossaryDetailsDisplay> implements
        GlossaryDetailsDisplay.Listener {
    private GetGlossaryDetailsResult glossaryDetails;

    private GlossaryDetails selectedDetailEntry;

    private final UiMessages messages;

    private final CachingDispatchAsync dispatcher;

    private final UserWorkspaceContext userWorkspaceContext;

    private GlossaryDisplay.Listener glossaryListener;

    @Inject
    public GlossaryDetailsPresenter(final GlossaryDetailsDisplay display,
            final EventBus eventBus, final UiMessages messages,
            final CachingDispatchAsync dispatcher,
            final UserWorkspaceContext userWorkspaceContext) {
        super(display, eventBus);
        this.dispatcher = dispatcher;
        this.messages = messages;
        this.userWorkspaceContext = userWorkspaceContext;
    }

    @Override
    protected void onBind() {
        display.setListener(this);
    }

    public void show(final GlossaryResultItem item) {
        // request glossary details from the server
        dispatcher.execute(
                new GetGlossaryDetailsAction(item.getSourceIdList()),
                new AsyncCallback<GetGlossaryDetailsResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Log.error(caught.getMessage(), caught);
                    }

                    @Override
                    public void onSuccess(GetGlossaryDetailsResult result) {
                        glossaryDetails = result;
                        display.setSourceText(item.getSource());
                        display.getTargetText().setText(item.getTarget());
                        display.clearEntries();

                        int i = 1;
                        for (GlossaryDetails detailsItem : result
                                .getGlossaryDetails()) {
                            display.setSourceLabel(
                                            messages.glossarySourceTermLabel(detailsItem
                                                    .getSrcLocale().toString()));
                            display.setTargetLabel(
                                            messages.glossaryTargetTermLabel(detailsItem
                                                    .getTargetLocale()
                                                    .toString()));
                            display.addEntry(messages.entriesLabel(i));
                            i++;
                        }
                        selectEntry(0);
                        display.center();
                    }
                });
    }

    private void populateDisplayData() {
        display.setSrcRef(selectedDetailEntry.getSourceRef());
        display.setDescription(selectedDetailEntry.getDescription());
        display.setPos(selectedDetailEntry.getPos());
        display.setTargetComment(selectedDetailEntry.getTargetComment());
        display.setLastModifiedDate(selectedDetailEntry.getLastModifiedDate());
        display.setUrl(selectedDetailEntry.getUrl());
    }

    @Override
    public void selectEntry(int selected) {
        if (selected >= 0) {
            selectedDetailEntry =
                    glossaryDetails.getGlossaryDetails().get(selected);
        }
        populateDisplayData();
    }

    @Override
    protected void onUnbind() {
    }

    @Override
    public void onRevealDisplay() {
    }

    public void setGlossaryListener(GlossaryDisplay.Listener glossaryListener) {
        this.glossaryListener = glossaryListener;
    }

    /**
     * Facilitate unit testing. Will be no-op if in client(GWT compiled) mode.
     *
     */
    protected void setStatesForTest(GlossaryDetails selectedDetailEntry) {
        if (!GWT.isClient()) {
            this.selectedDetailEntry = selectedDetailEntry;
        }
    }
}
