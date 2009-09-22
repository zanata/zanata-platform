package org.fedorahosted.flies.rest.dto;

import java.util.List;

public interface IExtensible {

	public List<Object> getExtensions();
	public List<Object> getExtensions(boolean create);
	public boolean hasExtensions();
	
	public <T> T getExtension(Class<T> clz);
	
	public <T> T getOrAddExtension(Class<T> clz);
	
}
