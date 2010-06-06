package org.fedorahosted.flies.rest.dto.deprecated;

import java.util.List;

public interface IExtensible {

	public List<Object> getExtensions();
	
	public <T> T getExtension(Class<T> clz);
	
	public <T> T getOrAddExtension(Class<T> clz);
	
}
