package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.shared.auth.EditorClientId;
import com.google.inject.ImplementedBy;

@ImplementedBy(DistinctColorListImpl.class)
public interface DistinctColor
{
   String getOrCreateColor(EditorClientId editorClientId);

   void releaseColor(EditorClientId editorClientId);
}
