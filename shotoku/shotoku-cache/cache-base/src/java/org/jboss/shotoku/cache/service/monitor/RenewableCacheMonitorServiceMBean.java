/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.shotoku.cache.service.monitor;

import java.util.List;
import java.util.Set;

import org.jboss.shotoku.cache.*;
import org.jboss.shotoku.cache.service.RenewableCacheServiceMBean;

/**
 * 
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public interface RenewableCacheMonitorServiceMBean {
	public RenewableCacheServiceMBean getRenewableCacheService();
	public void setRenewableCacheService(RenewableCacheServiceMBean renewableCacheService);
	
	/**
	 * 
	 * @return Interval at which cache items will be checked, if there are no errors.
	 */
	public long getInterval();
	/**
	 * 
	 * @param interval Interval at which cache items will be checked, if there are no errors.
	 */
	public void setInterval(long interval);
	
	/**
	 * 
	 * @return Maximum number of alerts stored. After this number is exceeded, no new alerts
	 * are recorded.
	 */
	public int getMaximumNumberOfAlerts();
	/**
	 * 
	 * @param maximumNumberOfAlerts Maximum number of alerts stored. After this number is exceeded, no new alerts
	 * are recorded.
	 */
	public void setMaximumNumberOfAlerts(int maximumNumberOfAlerts);
	
	/**
	 * 
	 * @return How many times the interval of a cache item must be exceeded, before an 
	 * "key not updated" or "key too long in update" alert is issued.
	 */
	public int getUpdateAlertIntervalMultiplier();
	/**
	 * 
	 * @param updateAlertIntervalMultiplier How many times the interval of a cache item must be exceeded, before an 
	 * "key not updated" or "key too long in update" alert is issued.
	 */
	public void setUpdateAlertIntervalMultiplier(int updateAlertIntervalMultiplier);
	
	public void update();
    
    public void start();
    public void stop();
    
    /**
     * 
     * @param rcid {@link CacheItem}} for which to get alerts.
     * @return A list of alerts for the given {@link CacheItem}, sorted from the
     * newest alert, or null is there are no alerts for the given cache item.
     */
    public List<CacheAlert> getAlertsForCacheItem(CacheItemOperations<?,?> rcid);
    /**
     * Clears all alerts.
     */
    public void clearAlerts();
    /**
     * 
     * @return A set of {@link CacheItemOperations}, for which there are any alerts.
     */
    public Set<CacheItemOperations<?,?>> getCacheItemsWithAlerts();
    
    /**
     * 
     * @return Smtp server to use, when sending alert e-mails.
     */
    public String getSmtpServer();
    
    /**
     * 
     * @param smtpServer Smtp server to use, when sending alert e-mails.
     */
    public void setSmtpServer(String smtpServer);
    
    /**
     * 
     * @return Smtp user to use, when sending alert e-mails. If the server does not require
     * authentication, an empty string or null.
     */
    public String getSmtpUser();
    
    /**
     * 
     * @param smtpUser Smtp user to use, when sending alert e-mails. If the server does not require
     * authentication, an empty string or null.
     */
    public void setSmtpUser(String smtpUser);
    
    /**
     * 
     * @return Smtp password to use, when sending alert e-mails.
     */
    public String getSmtpPassword();
    
    /**
     * 
     * @param smtpPassword Smtp password to use, when sending alert e-mails.
     */
    public void setSmtpPassword(String smtpPassword);
    
    /**
     * 
     * @return E-mail address, to whicha alerts will be sent. If this is an empty string or null, no
     * attemps of sending alerts will be sent.
     */
    public String getAlertEmail();
    
    /**
     * 
     * @param alertEmail E-mail address, to whicha alerts will be sent. If this is an empty string or null, no
     * attemps of sending alerts will be sent.
     */
    public void setAlertEmail(String alertEmail);
}
