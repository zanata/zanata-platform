package org.fedorahosted.flies.webtrans.client.editor.table;

public interface CancelCallback<ColType>
{
   void onCancel(ColType cellValue);
}
