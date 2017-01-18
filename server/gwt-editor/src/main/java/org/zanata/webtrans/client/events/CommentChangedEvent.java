package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.event.shared.GwtEvent;

public class CommentChangedEvent extends GwtEvent<CommentChangedEventHandler> {
    public static final Type<CommentChangedEventHandler> TYPE = new Type<>();
    private final TransUnitId transUnitId;
    private final int commentCount;

    public CommentChangedEvent(TransUnitId transUnitId, int commentCount) {
        this.transUnitId = transUnitId;
        this.commentCount = commentCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public TransUnitId getTransUnitId() {
        return transUnitId;
    }

    public Type<CommentChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(CommentChangedEventHandler handler) {
        handler.onCommentChanged(this);
    }
}
