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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.shotoku.cache.CacheItemOperations;
import org.jboss.shotoku.cache.service.RenewableCacheServiceMBean;

/**
 * 
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class DummyRenewableCacheMonitorService implements RenewableCacheMonitorServiceMBean {
	public void clearAlerts() {
	
	}

	public List<CacheAlert> getAlertsForCacheItem(CacheItemOperations<?,?> rcid) {
		return null;
	}

	public Set<CacheItemOperations<?,?>> getCacheItemsWithAlerts() {
		return new HashSet<CacheItemOperations<?,?>>();
	}

	public long getInterval() {
		return 0;
	}

	public int getMaximumNumberOfAlerts() {
		return 0;
	}

	public RenewableCacheServiceMBean getRenewableCacheService() {
		return null;
	}

	public int getUpdateAlertIntervalMultiplier() {
		return 0;
	}

	public void setInterval(long interval) {
		
	}

	public void setMaximumNumberOfAlerts(int maximumNumberOfAlerts) {
		
	}

	public void setRenewableCacheService(RenewableCacheServiceMBean renewableCacheService) {
		
	}

	public void setUpdateAlertIntervalMultiplier(int updateAlertIntervalMultiplier) {
		
	}

	public void start() {
		
	}

	public void stop() {
		
	}

	public void update() {
		
	}

	public String getAlertEmail() {
		return null;
	}

	public String getSmtpPassword() {
		return null;
	}

	public String getSmtpServer() {
		return null;
	}

	public String getSmtpUser() {
		return null;
	}

	public void setAlertEmail(String alertEmail) {
		
	}

	public void setSmtpPassword(String smtpPassword) {
		
	}

	public void setSmtpServer(String smtpServer) {
		
	}

	public void setSmtpUser(String smtpUser) {
		
	}
}
