package org.zanata.webtrans.client.service;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SaveEventQueueTest {
    private SaveEventQueue queue;

    @Before
    public void setUp() throws Exception {
        queue = new SaveEventQueue();
    }

    private static TransUnitSaveEvent saveEvent(int id, int verNum,
            String newContent, String oldContent) {
        return new TransUnitSaveEvent(Lists.newArrayList(newContent),
                ContentState.NeedReview, new TransUnitId(id), verNum,
                Lists.newArrayList(oldContent));
    }

    private static void assertEventEquals(TransUnitSaveEvent one,
            TransUnitSaveEvent other) {
        assertThat(one.getStatus()).isEqualTo(other.getStatus());
        assertThat(one.getTargets()).isEqualTo(other.getTargets());
        assertThat(one.getOldContents()).isEqualTo(other.getOldContents());
        assertThat(one.getTransUnitId()).isEqualTo(other.getTransUnitId());
        assertThat(one.getVerNum()).isEqualTo(other.getVerNum());
    }

    private static TransUnitSaveEvent fromQueueAsEvent(
            List<SaveEventQueue.EventWrapper> eventQueue, int index) {
        return eventQueue.get(index).toEvent();
    }

    @Test
    public void testPush() throws Exception {
        TransUnitSaveEvent firstEvent = saveEvent(1, 0, "new", "old");
        queue.push(firstEvent);
        assertThat(queue.getEventQueue()).hasSize(1);
        // pushing another event with same id and version should replace the old
        // pending one
        TransUnitSaveEvent anotherEvent = saveEvent(1, 0, "newer", "new");
        queue.push(anotherEvent);
        assertThat(queue.getEventQueue()).hasSize(1);
        assertEventEquals(fromQueueAsEvent(queue.getEventQueue(), 0),
                anotherEvent);
        assertThat(queue.hasPending()).isTrue();
        // pushing another event but doesn't match previous one. It will get
        // discarded
        TransUnitSaveEvent invalidEvent =
                saveEvent(1, 0, "blah", "content don\'t match previous");
        queue.push(invalidEvent);
        assertThat(queue.getEventQueue()).hasSize(1);
        assertEventEquals(fromQueueAsEvent(queue.getEventQueue(), 0),
                anotherEvent);
        assertThat(queue.hasPending()).isTrue();
        // pushing another event with different id will not conflict with other
        // pending one
        TransUnitSaveEvent differentId = saveEvent(2, 1, "different id", "hi");
        queue.push(differentId);
        assertThat(queue.getEventQueue()).hasSize(2);
        assertEventEquals(fromQueueAsEvent(queue.getEventQueue(), 0),
                anotherEvent);
        assertEventEquals(fromQueueAsEvent(queue.getEventQueue(), 1),
                differentId);
        assertThat(queue.hasPending()).isTrue();
        // pushing an event with equal state as the saving event. It will be
        // discarded
        queue.getNextPendingForSaving(anotherEvent.getTransUnitId());
        queue.push(anotherEvent);
        assertThat(queue.getEventQueue()).hasSize(2);
        assertEventEquals(fromQueueAsEvent(queue.getEventQueue(), 0),
                anotherEvent);
        assertEventEquals(fromQueueAsEvent(queue.getEventQueue(), 1),
                differentId);
    }

    @Test
    public void testGetNextPendingForSaving() throws Exception {
        TransUnitSaveEvent firstEvent = saveEvent(1, 0, "new", "old");
        queue.push(firstEvent);
        TransUnitSaveEvent next =
                queue.getNextPendingForSaving(firstEvent.getTransUnitId());
        assertEventEquals(firstEvent, next);
        assertThat(queue.getEventQueue().get(0).isSaving()).isTrue();
        assertThat(queue.isSaving(firstEvent.getTransUnitId())).isTrue();
        // pushing another event won't replace saving event
        TransUnitSaveEvent anotherSave = saveEvent(1, 0, "newer", "new");
        queue.push(anotherSave);
        assertThat(queue.getEventQueue()).hasSize(2);
        assertThat(queue.isSaving(anotherSave.getTransUnitId())).isTrue();
    }

    @Test
    public void testRemoveSaved() throws Exception {
        TransUnitSaveEvent firstEvent = saveEvent(1, 0, "new", "old");
        queue.push(firstEvent);
        TransUnitSaveEvent goToSave =
                queue.getNextPendingForSaving(firstEvent.getTransUnitId());
        TransUnitSaveEvent pendingBeforeSave = saveEvent(1, 0, "newer", "new");
        queue.push(pendingBeforeSave);
        // after save success we remove saved event
        queue.removeSaved(goToSave, 1);
        assertThat(queue.getEventQueue()).hasSize(1);
        TransUnitSaveEvent pendingEventAfterSave =
                queue.getEventQueue().get(0).toEvent();
        assertThat(pendingEventAfterSave.getStatus())
                .isEqualTo(pendingBeforeSave.getStatus());
        assertThat(pendingEventAfterSave.getTargets())
                .isEqualTo(pendingBeforeSave.getTargets());
        assertThat(pendingEventAfterSave.getOldContents())
                .isEqualTo(pendingBeforeSave.getOldContents());
        assertThat(pendingEventAfterSave.getTransUnitId())
                .isEqualTo(pendingBeforeSave.getTransUnitId());
        // version should be changed
        assertThat(pendingEventAfterSave.getVerNum()).isEqualTo(1);
        assertThat(queue.hasPending()).isTrue();
    }

    @Test
    public void testRemoveAllPending() throws Exception {
        TransUnitSaveEvent saveEvent = saveEvent(1, 0, "new", "old");
        queue.push(saveEvent);
        queue.removeAllPending(saveEvent.getTransUnitId());
        assertThat(queue.getEventQueue()).isEmpty();
        assertThat(queue.hasPending()).isFalse();
    }
}
