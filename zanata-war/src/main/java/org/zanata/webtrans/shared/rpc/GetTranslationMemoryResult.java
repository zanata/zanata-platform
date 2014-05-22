package org.zanata.webtrans.shared.rpc;

import java.util.List;

import org.zanata.webtrans.shared.model.TransMemoryResultItem;

public class GetTranslationMemoryResult implements DispatchResult {

    private static final long serialVersionUID = 1L;

    private GetTranslationMemory request;
    private List<TransMemoryResultItem> transmemories;

    @SuppressWarnings("unused")
    private GetTranslationMemoryResult() {
    }

    public GetTranslationMemoryResult(GetTranslationMemory request,
            List<TransMemoryResultItem> transmemories) {
        this.request = request;
        this.transmemories = transmemories;
    }

    /**
     * @return the request
     */
    public GetTranslationMemory getRequest() {
        return request;
    }

    public List<TransMemoryResultItem> getMemories() {
        return transmemories;
    }
}
