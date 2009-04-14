package org.jboss.shotoku.cache.admin;

import java.util.ArrayList;
import java.util.List;

import org.jboss.shotoku.cache.CacheItemOperations;
import org.jboss.shotoku.cache.service.monitor.DummyRenewableCacheMonitorService;
import org.jboss.shotoku.cache.service.monitor.RenewableCacheMonitorServiceMBean;
import org.jboss.shotoku.tools.CacheTools;

public class MonitorBean {
	private RenewableCacheMonitorServiceMBean monitor;
	
	private boolean monitorAvailable;
	private String monitorMbeanName;
	
	public RenewableCacheMonitorServiceMBean getMonitor() {
		if (monitor == null) {
			String mbean = getMonitorMbeanName();
			if (mbean == null) {
				mbean = CacheTools.DEFAULT_CACHE_MONITOR_MBEAN;
			}
			
			try {
				monitor = (RenewableCacheMonitorServiceMBean) CacheTools.getService(mbean, RenewableCacheMonitorServiceMBean.class);
				monitorAvailable = true;
			} catch (Exception e) {
				monitor = new DummyRenewableCacheMonitorService();
				monitorAvailable = false;
			}
		}
		
		return monitor;
	}
	
	public String getMonitorMbeanName() {
		return monitorMbeanName;
	}

	public void setMonitorMbeanName(String monitorMbeanName) {
		this.monitorMbeanName = monitorMbeanName;
	}
	
	public boolean getMonitorAvailable() {
		getMonitor();
		return monitorAvailable;
	}
	
	private List<String> currentAlerts;
	
    public List<String> getCurrentAlerts() {
    	if (currentAlerts == null) {
    		currentAlerts = new ArrayList<String>();
    		for (CacheItemOperations<?,?> rcid : getMonitor().getCacheItemsWithAlerts()) {
    			currentAlerts.add(rcid.getName());
    		}
    	}
    	
    	return currentAlerts;
    }
    
    public int getCurrentAlertsSize() {
    	return getCurrentAlerts().size();
    }
    
    public String clearAlerts() {
    	getMonitor().clearAlerts();
    	currentAlerts = null;
    	return null;
    }
    
    /*
     * Configuration
     */
    
    private long newInterval;
    private int newMaximumNumberOfAlerts;
    private int newUpdateAlertIntervalMultiplier;
    
    private String newAlertEmail;
	private String newSmtpPassword;
	private String newSmtpUser;
	private String newSmtpServer;
    
    public long getInterval() {
    	return getMonitor().getInterval();
    }
    
    public void setInterval(long interval) {
    	newInterval = interval;
    }
    
    public int getMaximumNumberOfAlerts() {
    	return getMonitor().getMaximumNumberOfAlerts();
    }
    
    public void setMaximumNumberOfAlerts(int maximumNumberOfAlerts) {
    	newMaximumNumberOfAlerts = maximumNumberOfAlerts;
    }
    
    public int getUpdateAlertIntervalMultiplier() {
    	return getMonitor().getUpdateAlertIntervalMultiplier();
    }
    
    public void setUpdateAlertIntervalMultiplier(int updateAlertIntervalMultiplier) {
    	newUpdateAlertIntervalMultiplier = updateAlertIntervalMultiplier;
    }

	public String getAlertEmail() {
		return getMonitor().getAlertEmail();
	}

	public void setAlertEmail(String alertEmail) {
		this.newAlertEmail = alertEmail;
	}

	public String getSmtpPassword() {
		return getMonitor().getSmtpPassword();
	}

	public void setSmtpPassword(String smtpPassword) {
		this.newSmtpPassword = smtpPassword;
	}

	public String getSmtpServer() {
		return getMonitor().getSmtpServer();
	}

	public void setSmtpServer(String smtpServer) {
		this.newSmtpServer = smtpServer;
	}

	public String getSmtpUser() {
		return getMonitor().getSmtpUser();
	}

	public void setSmtpUser(String smtpUser) {
		this.newSmtpUser = smtpUser;
	}
    
    public String updateServiceConfig() {
    	getMonitor().setInterval(newInterval);
    	getMonitor().setMaximumNumberOfAlerts(newMaximumNumberOfAlerts);
    	getMonitor().setUpdateAlertIntervalMultiplier(newUpdateAlertIntervalMultiplier);
    	
    	getMonitor().setSmtpServer(newSmtpServer);
    	getMonitor().setSmtpUser(newSmtpUser);
    	getMonitor().setSmtpPassword(newSmtpPassword);
    	getMonitor().setAlertEmail(newAlertEmail);
    
    	CacheFacesTools.addTimedFacesMessage("Monitor configuration changed successfully.");
    	
    	return null;
    }
}
