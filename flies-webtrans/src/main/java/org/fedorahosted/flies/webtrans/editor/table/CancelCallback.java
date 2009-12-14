package org.fedorahosted.flies.webtrans.editor.table;


public interface CancelCallback<ColType> {
    void onCancel(ColType cellValue);
 }
