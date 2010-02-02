package org.fedorahosted.flies.webtrans.editor.table;

import org.fedorahosted.flies.gwt.model.TransUnit;

public interface EditRowCallback {
    void gotoRow(int row);
    void gotoNextFuzzy(int row);
    void gotoPrevFuzzy(int row);
}
