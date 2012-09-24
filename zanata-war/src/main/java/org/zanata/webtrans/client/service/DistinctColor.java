package org.zanata.webtrans.client.service;

import org.zanata.webtrans.shared.auth.EditorClientId;
import com.google.inject.ImplementedBy;

@ImplementedBy(DistinctColorListImpl.class)
public interface DistinctColor
{
   String getOrCreateColor(EditorClientId editorClientId);

   void releaseColor(EditorClientId editorClientId);
}
