/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.webtrans.client.presenter;

import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Error;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Info;

import java.util.Collection;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.client.ui.TransMemoryMergePopupPanelDisplay;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.MergeOptions;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;
import org.zanata.webtrans.shared.util.StringNotEmptyPredicate;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergePresenter extends
        WidgetPresenter<TransMemoryMergePopupPanelDisplay> implements
        TransMemoryMergePopupPanelDisplay.Listener {

    private TransMemoryMergePopupPanelDisplay display;
    private final EventBus eventBus;
    private final CachingDispatchAsync dispatcher;
    private final NavigationService navigationService;
    private final UiMessages messages;
    private final Provider<UndoLink> undoLinkProvider;

    @Inject
    public TransMemoryMergePresenter(TransMemoryMergePopupPanelDisplay display,
            EventBus eventBus, CachingDispatchAsync dispatcher,
            NavigationService navigationService, UiMessages messages,
            Provider<UndoLink> undoLinkProvider) {
        super(display, eventBus);
        this.display = display;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.navigationService = navigationService;
        this.messages = messages;
        this.undoLinkProvider = undoLinkProvider;
        display.setListener(this);
    }

    @Override
    public void proceedToMergeTM(int percentage, MergeOptions mergeOptions) {
        Collection<TransUnit> items = getNotTranslatedItems();

        if (items.isEmpty()) {
            eventBus.fireEvent(new NotificationEvent(Info, messages
                    .noTranslationToMerge()));
            display.hide();
            return;
        }

        display.showProcessing();
        TransMemoryMerge action =
                prepareTMMergeAction(items, percentage, mergeOptions);
        dispatcher.execute(action, new AsyncCallback<UpdateTransUnitResult>() {
            @Override
            public void onFailure(Throwable caught) {
                Log.warn("TM merge failed", caught);
                eventBus.fireEvent(new NotificationEvent(Error, messages
                        .mergeTMFailed()));
                display.hide();
            }

            @Override
            public void onSuccess(final UpdateTransUnitResult result) {
                if (result.getUpdateInfoList().isEmpty()) {
                    eventBus.fireEvent(new NotificationEvent(Info, messages
                            .noTranslationToMerge()));
                } else {
                    final UndoLink undoLink = undoLinkProvider.get();
                    undoLink.prepareUndoFor(result);

                    List<String> rowIndicesOrNull =
                            Lists.transform(result.getUpdateInfoList(),
                                    SuccessRowIndexOrNullFunction.FUNCTION);
                    Iterable<String> successRowIndices =
                            Iterables.filter(rowIndicesOrNull,
                                    StringNotEmptyPredicate.INSTANCE);

                    Log.info("number of rows auto filled by TM merge: "
                            + Iterables.size(successRowIndices));
                    NotificationEvent event =
                            new NotificationEvent(Info, messages
                                    .mergeTMSuccess(Lists
                                            .newArrayList(successRowIndices)),
                                    undoLink);
                    eventBus.fireEvent(event);
                }
                display.hide();
            }
        });
    }

    private Collection<TransUnit> getNotTranslatedItems() {
        // TODO rhbz953734 - need to review this
        List<TransUnit> currentItems = navigationService.getCurrentPageValues();
        return Collections2.filter(currentItems, new Predicate<TransUnit>() {
            @Override
            public boolean apply(TransUnit input) {
                return !input.getStatus().isTranslated();
            }
        });
    }

    /**
     * See {@link org.zanata.model.type.TranslationSourceType#TM_MERGE}
     */
    private final String sourceType = "TM";

    private TransMemoryMerge prepareTMMergeAction(
            Collection<TransUnit> untranslatedTUs, int threshold,
            MergeOptions mergeOptions) {
        List<TransUnitUpdateRequest> updateRequests =
                Lists.newArrayList(Collections2.transform(untranslatedTUs,
                        from -> new TransUnitUpdateRequest(from.getId(),
                                null, null, from.getVerNum(), sourceType)));
        return new TransMemoryMerge(threshold, updateRequests, mergeOptions);
    }

    @Override
    public void cancelMergeTM() {
        display.hide();
    }

    public void prepareTMMerge() {
        display.showForm();
    }

    @Override
    protected void onBind() {
    }

    @Override
    protected void onUnbind() {
    }

    @Override
    protected void onRevealDisplay() {
    }

    private enum SuccessRowIndexOrNullFunction implements
            Function<TransUnitUpdateInfo, String> {
        FUNCTION;
        @Override
        public String apply(TransUnitUpdateInfo input) {
            if (input.isSuccess()) {
                return String.valueOf(input.getTransUnit().getRowIndex());
            }
            return "";
        }
    }
}
