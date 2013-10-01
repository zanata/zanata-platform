package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.GlossaryDetails;

public class UpdateGlossaryTermResult implements DispatchResult {

    private static final long serialVersionUID = 1L;

    private GlossaryDetails detail;

    @SuppressWarnings("unused")
    private UpdateGlossaryTermResult() {
        this(null);
    }

    public UpdateGlossaryTermResult(GlossaryDetails detail) {
        this.detail = detail;
    }

    public GlossaryDetails getDetail() {
        return detail;
    }
}
