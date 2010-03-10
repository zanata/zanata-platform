package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.common.ContentState;

public interface EditRowCallback {
    void gotoNextRow(int row);
    void gotoPrevRow(int row);
    void gotoNextFuzzy(int row, ContentState state);
    void gotoPrevFuzzy(int row, ContentState state);
}
