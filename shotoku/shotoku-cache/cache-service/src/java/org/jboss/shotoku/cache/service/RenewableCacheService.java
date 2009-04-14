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

import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.cache.TreeCacheMBean;
import org.jboss.shotoku.cache.CacheItemOperations;
import org.jboss.shotoku.cache.SignalExitUpdateThreadData;
import org.jboss.shotoku.cache.UpdateThread;
import org.jboss.shotoku.cache.UpdateThreadData;
import org.jboss.shotoku.tools.ConcurrentSet;
import org.jboss.shotoku.tools.ConcurrentHashSet;
import org.jboss.shotoku.tools.CacheTools;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class RenewableCacheService implements RenewableCacheServiceMBean {
    private final static Logger log = Logger.getLogger(RenewableCacheServiceMBean.class);
    
    private TreeCacheMBean treeCache;
    private long interval;
    private long lastUpdate;
    private Thread updateThread;
    private RenewableCacheStatistics statistics;
    
    /*
     * Service-handling functions.
     */

    public void start() {
    	log.info("Starting main update thread.");
    	
    	startUpdateThread();
    	
    	statistics = new RenewableCacheStatisticsImpl();
    }

    public void stop() {
        getUpdateThread().interrupt();
        
        log.info("Signaled the main update thread to stop.");
    }
    
    /*
     * 
     */
    
	public TreeCacheMBean getTreeCache() {
		return treeCache;
	}

	public void setTreeCache(TreeCacheMBean treeCache) {
		this.treeCache = treeCache;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public Thread getUpdateThread() {
		return updateThread;
	}

	public void setUpdateThread(Thread updateThread) {
		this.updateThread = updateThread;
	}
	
	public RenewableCacheStatistics getStatistics() {
		return statistics;
	}
	
	/*
	 * 
	 */

	private void startUpdateThread() {
        Thread ut = new Thread() {
            {
                setDaemon(true);
            }

            public void run() {
                while (true) {
                    try {
                        sleep(getInterval());
                    } catch (InterruptedException e) {
                    	// Quit.
                        log.info("Stopping update thread (interrupted).");
                        return;
                    }

                    try {
                        update();
                    } catch (Throwable t) {
                        // Making sure that an exception won't stop the thread.
                        log.error("Update method threw an exception.", t);
                    }

                    setLastUpdate(Calendar.getInstance().getTimeInMillis());
                }
            }
        };
        
        ut.start();
        setUpdateThread(ut);
    }
	
    /*
     * Cache handling
     */

    private final ConcurrentSet<CacheItemOperations<?,?>> cacheItems =
            new ConcurrentHashSet<CacheItemOperations<?,?>>();

	public Object get(Fqn fqn, Object key) throws CacheException {
		return treeCache.get(fqn, key);
	}

	public void put(Fqn fqn, Object key, Object o) throws CacheException {
		treeCache.put(fqn, key, o);
	}

	public void remove(Fqn fqn, Object key) throws CacheException {
		treeCache.remove(fqn, key);
	}

    public void register(CacheItemOperations<?,?> cacheItem) {
        cacheItems.add(cacheItem);
    }
    
    public void unregister(CacheItemOperations<?,?> cacheItem) throws CacheException {
    	cacheItems.remove(cacheItem);
    	
    	for (Object key : cacheItem.getKeysUpdates().keySet()) {
    		remove(cacheItem.getFqn(), key);
    	}
    }
   
    public Set<? extends CacheItemOperations<?,?>> getCacheItemsOperations() {
		return cacheItems;
	}

	private int counter = 0;
    private final Object counterSync = new Object();

    public Fqn generateNextFqn() {
        int c;
        synchronized(counterSync) { c = counter++; }

        return new Fqn(new Object[] {CacheTools.GENERATED_FQN_BASE, c});
    }

    /*
     * Update threads management.
     */

    private final LinkedBlockingQueue<UpdateThreadData<?,?>> updateThreadDataQueue =
        new LinkedBlockingQueue<UpdateThreadData<?,?>>();
    
    private int updateThreadCount;
    private int busyThreads;
    private int idleThreads;
    
    private final Object threadCounterSynchronizer = new Object();

    public void addUpdateThreadData(UpdateThreadData<?,?> data) {
    	updateThreadDataQueue.offer(data);
    }
    
    public int getUpdateThreadCount() {
        return updateThreadCount;
    }

    public synchronized void setUpdateThreadCount(int n) {
		if (updateThreadCount < n) {
			for (int i = updateThreadCount; i < n; i++) {
				UpdateThread ut = new UpdateThread(this, updateThreadDataQueue);
				ut.start();
			}
		} else if (n < updateThreadCount) {
			for (int i = updateThreadCount; i > n; i--) {
				updateThreadDataQueue.offer(new SignalExitUpdateThreadData<Object,Object>());
			}
		}

		log.info("Update thread count set to: " + n + ".");
		updateThreadCount = n;
	}
    
    public int getCurrentQueueSize() {
    	return updateThreadDataQueue.size();
    }
    
	public int getBusyThreadCount() {
		return busyThreads;
	}

	public int getIdleThreadCount() {
		return idleThreads;
	}

	public void reportThreadBusy() {
		synchronized (threadCounterSynchronizer) {
			busyThreads++;
			idleThreads--;
		}
	}

	public void reportThreadIdle() {
		synchronized (threadCounterSynchronizer) {
			busyThreads--;
			idleThreads++;
		}
	}

	public void reportThreadNew() {
		synchronized (threadCounterSynchronizer) {
			idleThreads++;
		}
	}

	public void reportThreadExit() {
		synchronized (threadCounterSynchronizer) {
			busyThreads--;
		}
	}

    /*
	 * Update function.
	 */

    public void update() {
        for (CacheItemOperations<?,?> sci : cacheItems) {
            try {
                sci.update();
            } catch (Throwable t) {
                log.error("Exception while updating a cache item.", t);
            }
        }
    }
}
