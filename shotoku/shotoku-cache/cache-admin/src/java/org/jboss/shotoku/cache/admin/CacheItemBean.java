package org.jboss.shotoku.cache.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.cache.CacheException;
import org.jboss.cache.Node;
import org.jboss.shotoku.cache.CacheItemOperations;
import org.jboss.shotoku.cache.service.RenewableCacheServiceMBean;
import org.jboss.shotoku.cache.service.monitor.CacheAlert;

public class CacheItemBean {
	private CacheItemOperations<?,?> cacheItem;
	private List<? extends Object> keysDuringUpdate;
	private List<? extends Object> keysNotDuringUpdate;
	private Map<Object, Long> keysUpdatesAgo;
	private RenewableCacheServiceMBean service;
	
	private int newId;
	private long newInterval;
	
	private AdminBean adminBean;
	
	public CacheItemBean(CacheItemOperations<?,?> cacheItem, RenewableCacheServiceMBean service, AdminBean adminBean) {
		this.cacheItem = cacheItem;
		this.service = service;
		
		this.adminBean = adminBean;
		
		keysDuringUpdate = new ArrayList<Object>(cacheItem.getKeysDuringUpdate());
		
		//
		
		Map<?, Long> keysUpdates = cacheItem.getKeysUpdates();		
		long now = System.currentTimeMillis();
		keysUpdatesAgo = new HashMap<Object, Long>();
		for (Object key : keysUpdates.keySet()) {
			long lastKeyUpdate = (now-keysUpdates.get(key));
			keysUpdatesAgo.put(key, lastKeyUpdate/1000);
		}
		
		//
		
		long interval = cacheItem.getInterval();
		if (interval == 0) {
			interval = service.getInterval();
		}
		
		//
		
		keysNotDuringUpdate = new ArrayList<Object>(keysUpdates.keySet());
		keysNotDuringUpdate.removeAll(keysDuringUpdate);
	}
	
	public String getName() {
		return cacheItem.getName();
	}
	
	public String getInfo() {
		return cacheItem.getInfo();
	}
	
	public List<? extends Object> getKeysDuringUpdate() {
		return keysDuringUpdate;
	}
	
	public List<? extends Object> getKeysNotDuringUpdate() {
		return keysNotDuringUpdate;
	}
	
	public Map<Object, Long> getLastUpdatesAgo() {
		return keysUpdatesAgo;
	}
	
	public String getFqn() {
		return cacheItem.getFqn().toString();
	}
	
	public int getFqnKeysCount() {
		try {
			Node n = service.getTreeCache().get(cacheItem.getFqn());
			if (n == null) {
				return 0;
			}
			
			return n.numAttributes();
		} catch (CacheException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<CacheAlert> getAlerts() {
		return adminBean.getMonitorBean().getMonitor().getAlertsForCacheItem(cacheItem);
	}

	public int getId() {
		return cacheItem.getId();
	}
	
	public void setId(int id) {
		newId = id;
	}
	
	public long getInterval() {
		return cacheItem.getInterval();
	}
	
	public void setInterval(long interval) {
		newInterval = interval;
		System.out.println("Setting interval to: " + interval);
	}
	
	private boolean checkId() {
		if (newId != getId()) {
			CacheFacesTools.addTimedFacesMessage("New or removed cache items. Please " +
					"refresh the page and try again.");
			return false;
		}
		
		return true;
	}
	
	public String updateConfig() {
		if (!checkId()) {
			return null;
		}
		
		cacheItem.setInterval(newInterval);
		
		CacheFacesTools.addTimedFacesMessage("Cache item " + getName() + " interval changed successfully.");
		
		return null;
	}

	public String reset() {
		if (!checkId()) {
			return null;
		}
		
		Object key = adminBean.getKeysInUpdateData().getRowData();
		
		cacheItem.resetKey(key);
		
		CacheFacesTools.addTimedFacesMessage("Key " + key + " has been reset.");
		
		return null;
	}
}
