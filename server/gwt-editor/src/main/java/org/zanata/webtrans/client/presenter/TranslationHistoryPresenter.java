package org.zanata.webtrans.client.presenter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.ReviewCommentEvent;
import org.zanata.webtrans.client.events.ReviewCommentEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.GetTransUnitActionContextHolder;
import org.zanata.webtrans.client.ui.TranslationHistoryDisplay;
import org.zanata.webtrans.shared.model.ComparableByDate;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.AddReviewCommentAction;
import org.zanata.webtrans.shared.rpc.AddReviewCommentResult;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryAction;
import org.zanata.webtrans.shared.rpc.GetTranslationHistoryResult;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TranslationHistoryPresenter extends
        WidgetPresenter<TranslationHistoryDisplay> implements
        TranslationHistoryDisplay.Listener, ReviewCommentEventHandler {
    private final TranslationHistoryDisplay display;
    private final EventBus eventBus;
    private final CachingDispatchAsync dispatcher;
    private final WebTransMessages messages;
    private final GetTransUnitActionContextHolder contextHolder;
    private final KeyShortcutPresenter keyShortcutPresenter;
    private TargetContentsPresenter targetContentsPresenter;
    private TransUnitId transUnitId;
    private ComparingPair comparingPair = ComparingPair.empty();

    @Inject
    public TranslationHistoryPresenter(TranslationHistoryDisplay display,
            EventBus eventBus, CachingDispatchAsync dispatcher,
            WebTransMessages messages,
            GetTransUnitActionContextHolder contextHolder,
            KeyShortcutPresenter keyShortcutPresenter) {
        super(display, eventBus);
        this.display = display;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.messages = messages;
        this.contextHolder = contextHolder;
        this.keyShortcutPresenter = keyShortcutPresenter;

        display.setListener(this);
        eventBus.addHandler(ReviewCommentEvent.TYPE, this);
        registerKeyShortcut();
    }

    private void registerKeyShortcut() {
        KeyShortcut confirmShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.CTRL_KEY, KeyCodes.KEY_ENTER))
                        .setContext(ShortcutContext.TransHistoryPopup)
                        .setHandler(event -> addComment(display.getComment())).build();
        keyShortcutPresenter.register(confirmShortcut);
    }

    @Override
    public void onShowReviewComment(ReviewCommentEvent event) {
        showTranslationHistory(event.getTransUnitId());
    }

    public void showTranslationHistory(final TransUnitId transUnitId) {
        this.transUnitId = transUnitId;
        popupAndShowLoading(messages.translationHistory());
        dispatcher.execute(new GetTranslationHistoryAction(transUnitId),
                new AsyncCallback<GetTranslationHistoryResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Log.error("failure getting translation history", caught);
                        eventBus.fireEvent(new NotificationEvent(
                                NotificationEvent.Severity.Error, caught
                                        .getMessage()));
                        display.hide();
                    }

                    @Override
                    public void onSuccess(GetTranslationHistoryResult result) {
                        Log.info("get back " + result.getHistoryItems().size()
                                + " trans history for id:" + transUnitId);
                        displayEntries(result.getLatest(),
                                result.getHistoryItems(),
                                result.getReviewComments());
                    }
                });
    }

    protected void popupAndShowLoading(String title) {
        comparingPair = ComparingPair.empty();
        display.setTitle(title);
        display.resetView();
        display.center();
    }

    protected void displayEntries(TransHistoryItem latest,
            List<TransHistoryItem> otherEntries,
            List<ReviewComment> reviewComments) {
        List<ComparableByDate> all = Lists.newArrayList();
        if (latest != null) {
            // add indicator for latest version
            all.add(latest.setOptionalTag(messages.latest()));
            List<String> newTargets = targetContentsPresenter.getNewTargets();
            if (!Objects.equal(latest.getContents(), newTargets)) {
                all.add(new TransHistoryItem(messages.unsaved(), newTargets,
                        null, messages.you(), new Date(), latest.getRevisionComment()));
            }
        }
        all.addAll(otherEntries);
        all.addAll(reviewComments);
        Collections.sort(all, Collections.reverseOrder());
        display.setData(all);
        // if transunitId is null, it's called by
        // org.zanata.webtrans.client.presenter.TransUnitsTablePresenter.refreshRow
        // in concurrent editing conflict. We will hide the comment field.
        display.enableComment(transUnitId != null);
    }

    @Override
    public void addComment(String commentContent) {
        dispatcher.execute(new AddReviewCommentAction(transUnitId,
                commentContent, contextHolder.getContext().getDocument()
                        .getId()),
                new AbstractAsyncCallback<AddReviewCommentResult>() {
                    @Override
                    public void onSuccess(AddReviewCommentResult result) {
                        display.addCommentToList(result.getComment());
                        display.clearInput();
                    }
                });
    }

    @Override
    public void copyIntoEditor(List<String> contents) {
        eventBus.fireEvent(new CopyDataToEditorEvent(contents));
    }

    @Override
    public void compareClicked(TransHistoryItem item) {
        comparingPair = comparingPair.tryAddOrRemoveIfExists(item);
        if (comparingPair.isFull()) {
            display.showDiff(comparingPair.one(), comparingPair.two(), messages
                    .translationHistoryComparison(comparingPair.one()
                            .getVersionNum(), comparingPair.two()
                            .getVersionNum()));
        } else {
            display.disableComparison();
        }
    }

    @Override
    public boolean isItemInComparison(TransHistoryItem item) {
        return comparingPair.contains(item);
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

    public void setCurrentValueHolder(
            TargetContentsPresenter targetContentsPresenter) {
        this.targetContentsPresenter = targetContentsPresenter;
    }

}
