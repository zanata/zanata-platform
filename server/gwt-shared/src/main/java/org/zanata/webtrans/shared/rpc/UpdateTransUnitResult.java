package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;

import com.google.common.base.Objects;

public class UpdateTransUnitResult implements DispatchResult {

    private static final long serialVersionUID = 1L;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private List<TransUnitUpdateInfo> tuUpdateInfo;

    public UpdateTransUnitResult() {
        tuUpdateInfo = new ArrayList<TransUnitUpdateInfo>();
    }

    public UpdateTransUnitResult(TransUnitUpdateInfo updateInfo) {
        this();
        addUpdateResult(updateInfo);
    }

    public void addUpdateResult(TransUnitUpdateInfo updateInfo) {
        tuUpdateInfo.add(updateInfo);
    }

    public List<TransUnitUpdateInfo> getUpdateInfoList() {
        return tuUpdateInfo;
    }

    public Integer getSingleVersionNum() {
        return getSingleUpdateInfo().getTransUnit().getVerNum();
    }

    public boolean isSingleSuccess() {
        return getSingleUpdateInfo().isSuccess();
    }

    private TransUnitUpdateInfo getSingleUpdateInfo() {
        if (tuUpdateInfo.size() == 1) {
            return tuUpdateInfo.get(0);
        } else {
            throw new IllegalStateException(
                    "this method can only be used when checking results for a single TransUnit update");
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("tuUpdateInfo", tuUpdateInfo)
                .toString();
    }

    public boolean isAllSuccess() {
        for (TransUnitUpdateInfo transUnitUpdateInfo : tuUpdateInfo) {
            if (!transUnitUpdateInfo.isSuccess()) {
                return false;
            }
        }
        return true;
    }
}
