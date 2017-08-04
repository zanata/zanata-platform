package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.zanata.webtrans.shared.model.GlossaryDetails;

public class GetGlossaryDetailsResult implements DispatchResult {

    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings(value = "SE_BAD_FIELD")
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
