package net.openl10n.flies.rest.dto.deprecated;

import java.util.List;

@Deprecated
public interface IExtensible
{

   public List<Object> getExtensions();

   public <T> T getExtension(Class<T> clz);

   public <T> T getOrAddExtension(Class<T> clz);

}
