package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.shared.model.DocumentInfo;

public interface HasDocumentListListener
{
   void statsOptionChange();

   void fireCaseSensitiveToken(boolean value);

   void fireExactSearchToken(boolean value);

   void fireFilterToken(String value);

   void fireDocumentSelection(DocumentInfo doc);
}
