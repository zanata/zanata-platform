package org.jboss.shotoku.cache.test;

import org.jboss.shotoku.cache.CacheItemDataSource;
import org.jboss.shotoku.cache.ValueChange;
import org.jboss.shotoku.cache.ValueInit;

public class TestCacheItem implements CacheItemDataSource<String, Integer> {
	public ValueInit<? extends Integer> init(String key) {
		return ValueInit.realValue(0);
	}

	public ValueChange<Integer> update(String key, Integer currentObject) {
		return ValueChange.changeTo(currentObject + 1);
	}
	
	public String getInfo() {
		return null;
	}
}
