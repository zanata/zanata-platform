package org.zanata.webtrans.client.editor.table;

/**
 * This is mainly to abstract some methods out from InlineTargetCellEditor.
 * So that other newly created class won't have to depend on ScrollPagingTable classes.
 */
public interface TransUnitsEditModel
{

   void acceptFuzzyEdit();
}
