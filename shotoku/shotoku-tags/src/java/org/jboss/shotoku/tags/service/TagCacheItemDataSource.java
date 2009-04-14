package org.jboss.shotoku.tags.service;

import org.jboss.shotoku.cache.CacheItemDataSource;
import org.jboss.shotoku.cache.ValueChange;
import org.jboss.shotoku.cache.ValueInit;
import org.jboss.shotoku.tags.TagService;

public class TagCacheItemDataSource implements CacheItemDataSource<Object, Object> {
	private TagService service;
	
	public TagCacheItemDataSource(TagService service) {
		super();
		this.service = service;
	}

	public String getInfo() {
		return null;
	}

	public ValueInit<? extends Object> init(Object key) {
		return ValueInit.realValue(null);
	}

	public ValueChange<? extends Object> update(Object key, Object currentObject) {
		service.update();
		return ValueChange.noChange();
	}

}
