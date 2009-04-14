package org.jboss.shotoku.cache.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.shotoku.cache.CacheItemOperations;
import org.jboss.shotoku.cache.service.RenewableCacheServiceMBean;
import org.jboss.shotoku.cache.service.RenewableCacheStatistics;
import org.jboss.shotoku.tools.CacheTools;

public class AdminBean {
	private String mbeanName;

	private RenewableCacheServiceMBean service;
	
	private List<CacheItemBean> cacheItems;
	private long newServiceInterval;
	private int newUpdateThreadCount;
	
	private MonitorBean monitorBean;
	
	public AdminBean() {
		// For development only.
		((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getSession().invalidate();
	}
	
	public RenewableCacheServiceMBean getService() {
		if (service == null) {
			String mbean = getMbeanName();
			if (mbean == null) {
				mbean = CacheTools.DEFAULT_RENEWABLE_CACHE_MBEAN;
			}
			
			try {
				service = (RenewableCacheServiceMBean) CacheTools.getService(mbean, RenewableCacheServiceMBean.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return service;
	}
	
	private final static Comparator<CacheItemBean> cacheItemBeanComparator = new Comparator<CacheItemBean>() {
		public int compare(CacheItemBean arg0, CacheItemBean arg1) {
			int id1 = arg0.getId();
			int id2 = arg1.getId();
			
			if (id1 < id2) {
				return -1;
			}
			
			if (id1 == id2) {
				return 0;
			}
			
			return 1;
		}
		
	};
	
	public MonitorBean getMonitorBean() {
		return monitorBean;
	}

	public void setMonitorBean(MonitorBean monitorBean) {
		this.monitorBean = monitorBean;
	}

	public List<CacheItemBean> getCacheItems() {
		if (cacheItems == null) {
			cacheItems = new ArrayList<CacheItemBean>();
			
			for (CacheItemOperations<?,?> cacheItemData : getService().getCacheItemsOperations()) {
				CacheItemBean cacheItemBean = new CacheItemBean(cacheItemData, getService(), this);
				cacheItems.add(cacheItemBean);
			}
			
			Collections.sort(cacheItems, cacheItemBeanComparator);
		}
		
		return cacheItems;
	}

	public String getMbeanName() {
		return mbeanName;
	}

	public void setMbeanName(String mbeanName) {
		this.mbeanName = mbeanName;
	}

	public long getServiceLastUpdateSecondsAgo() {
    	return (System.currentTimeMillis() - getService().getLastUpdate())/1000;
    }
	
	public Date getServiceLastUpdateDate() {
        return new Date(getService().getLastUpdate());
    }
	
	public int getCurrentQueueSize() {
		return getService().getCurrentQueueSize();
	}
	
    public RenewableCacheStatistics getStatistics() {
    	return getService().getStatistics();
    }
    
    public int getBusyThreadCount() {
    	return getService().getBusyThreadCount();
    }
    
    public int getIdleThreadCount() {
    	return getService().getIdleThreadCount();
    }
    
    public int getUpdateThreadCount() {
    	return getService().getUpdateThreadCount();
    }
    
    public void setUpdateThreadCount(int newUpdateThreadCount) {
    	this.newUpdateThreadCount = newUpdateThreadCount;
    }
    
    public long getServiceInterval() {
    	return getService().getInterval();
    }
    
    public void setServiceInterval(long newServiceInterval) {
    	this.newServiceInterval = newServiceInterval;
    }
    
    public String updateServiceConfig() {
    	getService().setInterval(newServiceInterval);
    	getService().setUpdateThreadCount(newUpdateThreadCount);
    	
    	CacheFacesTools.addTimedFacesMessage("Service interval and update " +
    			"thread count changed successfully.");
    	
    	return null;
    }
    
    /**
     * Data table of a cache item. Used to retrieve a key, which has been selected.
     */
	private UIData keysInUpdateData;
	
	public UIData getKeysInUpdateData() {
		return keysInUpdateData;
	}

	public void setKeysInUpdateData(UIData keysInUpdateData) {
		this.keysInUpdateData = keysInUpdateData;
	}
}
