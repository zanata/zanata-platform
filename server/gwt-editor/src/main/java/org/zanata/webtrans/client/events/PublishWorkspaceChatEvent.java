package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData;

import com.google.gwt.event.shared.GwtEvent;

public class PublishWorkspaceChatEvent extends
        GwtEvent<PublishWorkspaceChatEventHandler> implements
        HasWorkspaceChatData {

    private final String personId;
    private final String timestamp;
    private final String msg;
    private final MESSAGE_TYPE messageType;

    public PublishWorkspaceChatEvent(HasWorkspaceChatData data) {
        personId = data.getPersonId();
        timestamp = data.getTimestamp();
        msg = data.getMsg();
        messageType = data.getMessageType();
    }

    /**
     * Handler type.
     */
    private static final Type<PublishWorkspaceChatEventHandler> TYPE = new Type<>();

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<PublishWorkspaceChatEventHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<PublishWorkspaceChatEventHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(PublishWorkspaceChatEventHandler handler) {
        handler.onPublishWorkspaceChat(this);
    }

    @Override
    public String getPersonId() {
        return personId;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public MESSAGE_TYPE getMessageType() {
        return messageType;
    }
}
