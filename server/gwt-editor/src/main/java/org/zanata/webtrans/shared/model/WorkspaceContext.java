package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class WorkspaceContext implements IsSerializable, Serializable {
    private static final long serialVersionUID = 1920634796042997648L;
    private WorkspaceId workspaceId;
    private String workspaceName;
    private String localeName;

    // for GWT
    @SuppressWarnings("unused")
    private WorkspaceContext() {
    }

    public WorkspaceContext(WorkspaceId workspaceId, String workspaceName,
            String localeName) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.localeName = localeName;
    }

    @Override
    public String toString() {
        return workspaceId.toString();
    }

    public WorkspaceId getWorkspaceId() {
        return workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public String getLocaleName() {
        return localeName;
    }
}
