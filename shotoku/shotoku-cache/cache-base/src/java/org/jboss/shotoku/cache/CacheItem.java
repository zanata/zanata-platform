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
package org.jboss.shotoku.cache;

import org.apache.log4j.Logger;
import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.shotoku.tools.CacheTools;
import org.jboss.shotoku.tools.ConcurrentSet;
import org.jboss.shotoku.tools.ConcurrentHashSet;
import org.jboss.shotoku.cache.service.RenewableCacheServiceMBean;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Use this class if you want to store objects in a cache that
 * will be updated on a regular intervals of time, by a service daemon
 * thread. All data is held in one node of a {@link org.jboss.cache.TreeCache}.
 * 
 * Instances of this class can be obtained using the {@link #create(CacheItemDataSource)}
 * and {@link #create(CacheItemDataSource, Fqn, String, long)} methods, provided a
 * {@link CacheItemDataSource}.
 * 
 * If a key is requested for the first time, it is initialized with the
 * {@link CacheItemDataSource#init(Object)} method. Later, the
 * {@link CacheItemDataSource#update(Object, Object)} method
 * is called to (possibly) update the currently held value.
 * 
 * The cache item will be auto-registered in the service upon construction 
 * --- so take care when constructing objects of this class. When you want to
 * remove all keys, from the TreeCache node, that are handled by this cache item,
 * simply call the {@link #unregister()} method.
 * 
 * @param <K> Type of the key of the objects held in cache. The keys
 * should bahave well as map keys (most probably, the hashCode() and
 * equals() methods should be overriden).
 * 
 * @param <T> Type of the objects that will be stored in the cache.
 * 
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class CacheItem<K, T> implements CacheItemOperations<K, T> {
	private final Logger log = Logger.getLogger(CacheItem.class);
	
	private CacheItemDataSource<K, T> dataSource;
	
    private Fqn fqn;
    private long interval;
    private String mbeanName;
    private int id;
    
    /**
     * A flag that indicates if this cache item is registered.
     */
    private volatile boolean registered;
    
    private ConcurrentMap<K, Long> keysUpdates;
    /**
     * Double-update-start prevention: if a key is already in that set, it won't be updated.
     */
    private ConcurrentSet<K> keysInUpdate;
    /**
     * Information: update methods of which keys are currently executed. Doesn't have to 
     * coincide with <code>keysInUpdate</code>, as the execution of the update methods for
     * some keys may be waiting in a queue.
     */
    private ConcurrentSet<K> keysDuringUpdate;
    /**
     * Exceptions whih occured on last update of keys.
     */
    private ConcurrentMap<K, Throwable> keysExceptions;
    
    private RenewableCacheServiceMBean service;

    private CacheItem(CacheItemDataSource<K, T> dataSource, Fqn fqn, String mbeanName, long interval) {
    	this.dataSource = dataSource;
    	
    	if (mbeanName == null) {
    		mbeanName = CacheTools.DEFAULT_RENEWABLE_CACHE_MBEAN;
    	}
    	
    	this.mbeanName = mbeanName;
    	
    	this.interval = interval;
    	
        keysUpdates = new ConcurrentHashMap<K, Long>();
        keysDuringUpdate = new ConcurrentHashSet<K>();
        keysInUpdate = new ConcurrentHashSet<K>();
        keysExceptions = new ConcurrentHashMap<K, Throwable>();
    	
        id = CacheTools.getNextId();
        
    	register();
        
    	if (fqn == null) {
    		this.fqn = service.generateNextFqn();
    	} else {
    		this.fqn = fqn;
    	}
    }
    
    /**
     * Creates and registeres a new cache item, with default parameter values (see
     * {@link #create(CacheItemDataSource, Fqn, String, long)}): the fqn will be auto-generated,
     * a default cache mbean name will be used, update will happen on each service thread
     * update and there will be no limit on the length of a key update.
     * @param dataSource Data source from which the resulting cache item will read.
     */
    public static <K,T> CacheItemUser<K,T> create(CacheItemDataSource<K,T> dataSource) {
    	return create(dataSource, null, null, 0);
    }
    
    /**
     * Creates and registeres a new cache item.
     * 
     * @param dataSource Data source from which the resulting cache item will read.
     *
     * @param fqn An fqn of the node in TreeCache, where data should be held. If it is null,
     * a unique fqn will be auto-generated for this cache item.
     * 
     * @param mbeanName A name of an {@link RenewableCacheServiceMBean} mbean, which
     * should be used to perform cache operations. This mbeans holds a reference
     * to a {@link org.jboss.cache.TreeCache}. If it is null, a default mbean
     * name will be used.
     * 
     * @param interval Interval at which the update operation will be executed.
     * Effectively, the interval will be rounded to the nearest multiplicity of
     * the service update thread interval. The interval should be given in milliseconds.
     * If it is 0, the {@link #update()} method will be executed on every service
     * thread update.
     */
    public static <K,T> CacheItemUser<K,T> create(CacheItemDataSource<K,T> dataSource,
    		Fqn fqn, String mbeanName, long interval) {
    	return new CacheItem<K, T>(dataSource, fqn, mbeanName, interval);
    }
    
    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemUser#register()
     */
    public synchronized void register() {
			synchronized (keysUpdates) {
				synchronized (keysDuringUpdate) {
					keysUpdates.clear();
					keysDuringUpdate.clear();
					
					try {
						service = (RenewableCacheServiceMBean) 
							CacheTools.getService(mbeanName, RenewableCacheServiceMBean.class);
					} catch (Exception e) {
						log.error("No RenewableCacheService bound to "
								+ mbeanName + " in cache item "
								+ getName() + "!");
						return;
					}

					service.register(this);
					
					registered = true;
				}
			}
	}

    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemUser#unregister()
     */
    public synchronized void unregister() {
        try {
        	// Setting this flag blocks any put()s into the cache, as well as makes calling get()
        	// throw an exception.
        	registered = false;
        	
        	service.unregister(this);
		} catch (CacheException e) {
			log.error("Error while unregistering a cache item " + this.getClass() + ".", e);
		}
    }
    
    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemUser#getFqn()
     */
    public Fqn getFqn() {
		return fqn;
	}

    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemUser#getInterval()
     */
	public synchronized long getInterval() {
		return interval;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.shotoku.cache.CacheItemUser#setInterval(long)
	 */
	public synchronized void setInterval(long interval) {
		this.interval = interval;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.shotoku.cache.CacheItemUser#getMbeanName()
	 */
	public String getMbeanName() {
		return mbeanName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.shotoku.cache.CacheItemUser#getKeysDuringUpdate()
	 */
	public ConcurrentSet<K> getKeysDuringUpdate() {
		return keysDuringUpdate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.shotoku.cache.CacheItemUser#getKeysUpdates()
	 */
	public ConcurrentMap<K, Long> getKeysUpdates() {
		return keysUpdates;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.shotoku.cache.CacheItemUser#getKeysExceptions()
	 */
	public ConcurrentMap<K, Throwable> getKeysExceptions() {
		return keysExceptions;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.shotoku.cache.CacheItemUser#getId()
	 */
	public int getId() {
		return id;
	}
    
    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemUser#getInfo()
     */
    public String getInfo() {
    	return dataSource.getInfo();
    }
    
    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemUser#getName()
     */
    public String getName() {
    	return dataSource.getClass().getName();
    }

	/**
     * Binds the given key with the given object in the associated TreeCache
     * node. Should be only called
     * from the {@link #update()} method to put new values in the
     * cache (if not called during an update, this method has no effect).
     * @param key Key of the object.
     * @param object Object that should be bound.
     */
    private synchronized final void put(K key, T object) { 
        if (!keysDuringUpdate.contains(key)) {
            return;
        }
        
        if (!registered) {
        	return;
        }
        
        try {
			service.put(fqn, key, object);
		} catch (CacheException e) {
			log.error("Error while putting a value for key \"" + key + "\" in cache item " + this.getClass() + ".", e);
		}
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemUser#get(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
	public final T get(K key) {
    	if (!keysUpdates.containsKey(key)) {
			synchronized (this) {
				if (!keysUpdates.containsKey(key)) {
					ValueInit<? extends T> ret = dataSource.init(key);
					try {
						if (!registered) {
			        		throw new UnregisteredException();
			        	}
						
						service.put(fqn, key, ret.getValue());
					} catch (CacheException e) {
						log.error("Error while putting a value for key \""
								+ key + "\" in cache item " + this.getClass()
								+ ".", e);
						return null;
					}

					// We have to initialize a new object.
					if (ret.hasRealValue()) {
						// First update after the cache item interval.
						keysUpdates.put(key, System.currentTimeMillis());
					} else {
						// First update as soon as possible.
						keysUpdates.put(key, (long) 0);
					}

					return ret.getValue();
				}
			}
		}

        try {
        	if (!registered) {
        		throw new UnregisteredException();
        	}
        	
			return (T) service.get(fqn, key);
		} catch (CacheException e) {
			log.error("Error while getting a value for key \"" + key + "\" in cache item " + this.getClass() + ".", e);
			return null;
		}
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemOperations#update()
     */
    public void update() {
        long now = Calendar.getInstance().getTimeInMillis();
        
        for (final K key : keysUpdates.keySet()) {
            if (now - keysUpdates.get(key) >= interval) {
                if (keysInUpdate.add(key)) {
                    service.addUpdateThreadData(new UpdateThreadData<K, T>(key, this) {
                        public void execute() {
                        	keysUpdates.put(key, System.currentTimeMillis());
                        	keysDuringUpdate.add(key);
                        	
                            try {
                                ValueChange<? extends T> change = dataSource.update(key, get(key));
                                if (change.hasValue()) {
                                	put(key, change.getValue());
                                }
                                
                                // Checking if there is anything to release.
                                if (change.getRelease() != null) {
                                	try {
                                		change.getRelease().release();
                                	} catch (Throwable t) {
                                		// Ignoring any exceptions.
                                	}
                                }
                            } catch (UnregisteredException e) {
                            } finally {
                            	keysInUpdate.remove(key);
                            	keysDuringUpdate.remove(key);
                            }
                        }
                    });
                }
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemUser#resetKey(java.lang.Object)
     */
    public void resetKey(Object key) {
    	keysInUpdate.remove(key);
    	keysDuringUpdate.remove(key);
    }
    
    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemOperations#reportUpdateOk(java.lang.Object)
     */
    public void reportUpdateOk(K key) {
		
	}

    /*
     * (non-Javadoc)
     * @see org.jboss.shotoku.cache.CacheItemOperations#reportUpdateWithException(java.lang.Object, java.lang.Throwable)
     */
	public void reportUpdateWithException(K key, Throwable t) {
		if (keysExceptions.put(key, t) == null) {
			// There was no exception before.
			log.error("Update of cache item " + getName() + " for key " + key + " threw an exception. Suppressing " +
					"subsequent exceptions.", t);
		}
	}
}
