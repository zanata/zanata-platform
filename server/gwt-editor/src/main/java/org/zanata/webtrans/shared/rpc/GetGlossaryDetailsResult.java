package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.GlossaryDetails;

public class GetGlossaryDetailsResult implements DispatchResult {

    private static final long serialVersionUID = 1L;

    private ArrayList<GlossaryDetails> glossaryDetails;

    @SuppressWarnings("unused")
    private GetGlossaryDetailsResult() {
        this(null);
    }

    public GetGlossaryDetailsResult(ArrayList<GlossaryDetails> glossaryDetails) {
        this.glossaryDetails = glossaryDetails;
    }

    public ArrayList<GlossaryDetails> getGlossaryDetails() {
        return glossaryDetails;
    }
}
