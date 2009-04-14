package org.jboss.shotoku.cache.test;

import org.jboss.shotoku.cache.CacheItemDataSource;
import org.jboss.shotoku.cache.ValueChange;
import org.jboss.shotoku.cache.ValueInit;

public class TestCacheItem2 implements CacheItemDataSource<String, Integer> {
	public ValueInit<? extends Integer> init(String key) {
		return ValueInit.realValue(0);
	}

	public ValueChange<Integer> update(String key, Integer currentObject) {
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return ValueChange.changeTo(currentObject + 1);
	}
	
	public String getInfo() {
		return null;
	}
}
