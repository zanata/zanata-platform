package net.openl10n.flies.model;

import java.util.Date;

import net.openl10n.flies.common.ContentType;

public interface IDocumentHistory
{

   HPerson getLastModifiedBy();

   Date getLastChanged();

   Integer getRevision();

   boolean isObsolete();

   HLocale getLocale();

   ContentType getContentType();

   String getName();

   String getPath();

}
