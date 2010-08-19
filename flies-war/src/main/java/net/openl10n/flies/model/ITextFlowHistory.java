package net.openl10n.flies.model;

public interface ITextFlowHistory
{

   Integer getPos();

   Integer getRevision();

   boolean isObsolete();

   String getContent();

}
