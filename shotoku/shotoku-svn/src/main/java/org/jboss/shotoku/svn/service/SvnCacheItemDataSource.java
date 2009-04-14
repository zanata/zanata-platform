package org.jboss.shotoku.svn.service;

import org.jboss.shotoku.cache.CacheItemDataSource;
import org.jboss.shotoku.cache.ValueChange;
import org.jboss.shotoku.cache.ValueInit;
import org.jboss.shotoku.svn.SvnService;

public class SvnCacheItemDataSource implements CacheItemDataSource<Object, Object> {
	private SvnService service;
	
	public SvnCacheItemDataSource(SvnService service) {
		super();
		this.service = service;
	}

	public String getInfo() {
		return service.getServiceInfo();
	}

	public ValueInit<? extends Object> init(Object key) {
		return ValueInit.realValue(null);
	}

	public ValueChange<? extends Object> update(Object key, Object currentObject) {
		service.update();
		return ValueChange.noChange();
	}

}
