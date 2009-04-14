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
package org.jboss.shotoku.cache.service;

import java.util.Set;

import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.cache.TreeCacheMBean;
import org.jboss.shotoku.cache.CacheItemOperations;
import org.jboss.shotoku.cache.UpdateThreadData;

/**
 * 
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public interface RenewableCacheServiceMBean {
    public Object get(Fqn fqn, Object key) throws CacheException;
    public void put(Fqn fqn, Object key, Object o) throws CacheException;
    public void remove(Fqn fqn, Object key) throws CacheException;
    
    public void register(CacheItemOperations<?,?> cacheItem);
    public void unregister(CacheItemOperations<?,?> cacheItem) throws CacheException;

    public Fqn generateNextFqn();

    public void addUpdateThreadData(UpdateThreadData<?, ?> data);
    public int getCurrentQueueSize(); 
    
    public RenewableCacheStatistics getStatistics();
    
    public TreeCacheMBean getTreeCache();
	public void setTreeCache(TreeCacheMBean treeCache);
	
	public long getInterval();
	public void setInterval(long interval);
	
	public long getLastUpdate();
	public void setLastUpdate(long lastUpdate);
	
	public int getUpdateThreadCount();
    public void setUpdateThreadCount(int n);
    
    public void reportThreadIdle();
    public void reportThreadBusy();
    public void reportThreadNew();
    public void reportThreadExit();
    
    public int getIdleThreadCount();
    public int getBusyThreadCount();
    
    public Set<? extends CacheItemOperations<?, ?>> getCacheItemsOperations();
    
    public void update();
    
    public void start();
    public void stop();
}
