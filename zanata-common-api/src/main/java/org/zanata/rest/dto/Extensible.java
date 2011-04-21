package org.zanata.rest.dto;

import org.zanata.rest.dto.resource.ExtensionSet;

public interface Extensible<T extends ExtensionValue>
{
   ExtensionSet<T> getExtensions();
}
