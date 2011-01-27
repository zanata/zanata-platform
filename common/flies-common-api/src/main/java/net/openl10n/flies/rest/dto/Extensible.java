package net.openl10n.flies.rest.dto;

import net.openl10n.flies.rest.dto.resource.ExtensionSet;

public interface Extensible<T extends ExtensionValue>
{
   ExtensionSet<T> getExtensions();
}
