package net.openl10n.flies.model;

import java.util.Date;

import net.openl10n.flies.common.ContentState;

public interface ITextFlowTargetHistory
{

   ContentState getState();

   Integer getTextFlowRevision();

   HPerson getLastModifiedBy();

   String getContent();

   Date getLastChanged();

   Integer getVersionNum();

}
