package org.zanata.webtrans.shared.rpc;

public class PublishWorkspaceChat implements SessionEventData,
        HasWorkspaceChatData {
    private static final long serialVersionUID = 1L;
    private String personId;
    private String timestamp;
    private String msg;
    private MESSAGE_TYPE messageType;

    public PublishWorkspaceChat(String personId, String timestamp, String msg,
            MESSAGE_TYPE messageType) {
        this.personId = personId;
        this.timestamp = timestamp;
        this.msg = msg;
        this.messageType = messageType;
    }

    // for ExposeEntity
    public PublishWorkspaceChat() {
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
