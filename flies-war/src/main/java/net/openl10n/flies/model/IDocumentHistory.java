package net.openl10n.flies.model;

import java.util.Date;

import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;

public interface IDocumentHistory
{

   HPerson getLastModifiedBy();

   Date getLastChanged();

   Integer getRevision();

   boolean isObsolete();

   LocaleId getLocale();

   ContentType getContentType();

   String getName();

   String getPath();

}
