package org.zanata.webtrans.shared.rpc;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;

public class GetTranslatorListResult implements DispatchResult {

    private static final long serialVersionUID = 1L;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private Map<EditorClientId, PersonSessionDetails> translatorlist;
    private int size;

    @SuppressWarnings("unused")
    private GetTranslatorListResult() {
    }

    public GetTranslatorListResult(
            Map<EditorClientId, PersonSessionDetails> translatorlist, int size) {
        this.translatorlist = translatorlist;
        this.size = size;
    }

    public Map<EditorClientId, PersonSessionDetails> getTranslatorList() {
        return translatorlist;
    }

    public int getSize() {
        return size;
    }
}
