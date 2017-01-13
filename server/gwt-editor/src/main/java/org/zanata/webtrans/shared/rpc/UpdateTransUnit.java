package org.zanata.webtrans.shared.rpc;

import com.google.common.base.MoreObjects;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated.UpdateType;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

public class UpdateTransUnit extends
        AbstractWorkspaceAction<UpdateTransUnitResult> {
    private static final long serialVersionUID = 1L;

    private List<TransUnitUpdateRequest> updateRequests;
    private UpdateType updateType;

    // needed for Gwt serialization
    @SuppressWarnings("unused")
    private UpdateTransUnit() {
    }

    public UpdateTransUnit(UpdateType updateType) {
        this.updateType = updateType;
        updateRequests = new ArrayList<TransUnitUpdateRequest>();
    }

    public UpdateTransUnit(TransUnitUpdateRequest updateRequest,
            UpdateType updateType) {
        this(updateType);
        updateRequests.add(updateRequest);
    }

    public void addTransUnit(TransUnitUpdateRequest updateRequest) {
        this.updateRequests.add(updateRequest);
    }

    public List<TransUnitUpdateRequest> getUpdateRequests() {
        return updateRequests;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("updateRequests", updateRequests)
                .add("updateType", updateType).toString();
    }
}
