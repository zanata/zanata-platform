package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.common.ContentState;

public interface EditRowCallback {
    void gotoRow(int row);
    void gotoNextFuzzy(int row, ContentState state);
    void gotoPrevFuzzy(int row, ContentState state);
}
