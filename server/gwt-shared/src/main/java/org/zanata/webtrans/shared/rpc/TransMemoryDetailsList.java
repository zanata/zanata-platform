package org.zanata.webtrans.shared.rpc;

import java.util.ArrayList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.zanata.webtrans.shared.model.TransMemoryDetails;

public class TransMemoryDetailsList implements DispatchResult {

    // generated
    private static final long serialVersionUID = 4068321598270485290L;

    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private ArrayList<TransMemoryDetails> items;

    @SuppressWarnings("unused")
    private TransMemoryDetailsList() {
        this(null);
    }

    public TransMemoryDetailsList(ArrayList<TransMemoryDetails> items) {
        this.items = items;
    }

    public ArrayList<TransMemoryDetails> getItems() {
        return items;
    }

}
