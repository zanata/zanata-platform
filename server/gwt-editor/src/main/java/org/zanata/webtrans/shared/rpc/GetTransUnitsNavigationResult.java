package org.zanata.webtrans.shared.rpc;

import java.util.List;
import java.util.Map;

import com.google.common.base.MoreObjects;
import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

public class GetTransUnitsNavigationResult implements IsSerializable {

    private List<TransUnitId> idIndexList;
    private Map<TransUnitId, ContentState> transIdStateList;

    @SuppressWarnings("unused")
    private GetTransUnitsNavigationResult() {
    }

    public GetTransUnitsNavigationResult(List<TransUnitId> idIndexList,
            Map<TransUnitId, ContentState> transIdStateList) {
        this.idIndexList = idIndexList;
        this.transIdStateList = transIdStateList;
    }

    public List<TransUnitId> getIdIndexList() {
        return idIndexList;
    }

    public Map<TransUnitId, ContentState> getTransIdStateList() {
        return transIdStateList;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("idIndexList", idIndexList)
                .add("transIdStateList", transIdStateList).toString();
    }
}
