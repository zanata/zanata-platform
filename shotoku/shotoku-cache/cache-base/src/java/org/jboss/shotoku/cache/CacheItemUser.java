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

import java.util.Map;
import java.util.Set;

import org.jboss.cache.Fqn;
import org.jboss.shotoku.cache.service.RenewableCacheServiceMBean;

/**
 * An interface presented to the user of the cache item. Its main function is
 * to retrieve values from the cache, but also to get configuration information and
 * information about the current state of keys (their last updates, etc) handeled
 * by this cache item.
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 *
 * @param <K> Type of the keys in the cache.
 * @param <T> Type of the values bound to the keys in the cache.
 */
public interface CacheItemUser<K, T> {
	/**
	 * Resets the given key, that is, it's update status. Hence, if a thread updating a key
	 * locks for some reason, it is possible to resume updates of this key. Use with caution.
	 * @param key Key, which update status should be reset.
	 */
	public void resetKey(Object key);
	
	/**
     * Gets an object that is bound to the given key in the associated
     * TreeCache node.
     * If this object is not in the cache, it will be initialized (using the
     * {@link CacheItemDataSource#init(Object)} method of a data source
     * that was passed to {@link CacheItem#create(CacheItemDataSource)}).
     * @param key Key of the object to get. Cannot be null.
     * @return Value of the object.
     */
	public T get(K key);
	
	/**
     * Registers this cache item in the service. This method is automatically called
     * when creating the cache item (using {@link CacheItem#create(CacheItemDataSource)}),
     * so you will only need to use it if you previously
     * manually unregistered the cache item using {@link #unregister()}.
     */
    public void register();
    
    /**
     * Removes all keys handled by this cache item from the associated TreeCache node and
     * stops updates on this cache item.
     */
    public void unregister();
    
	/**
	 * 
	 * @return A fqn of a TreeCache node associated with this cache item. This is a node in which
	 * data will be kept.
	 */
	public Fqn getFqn();
	
	/**
	 * 
	 * @return Interval at which updates of keys will be executed.
     * Effectively, the interval will be rounded to the nearest multiplicity of
     * the service update thread interval. The interval should be given in milliseconds.
     * If it is 0, the {@link CacheItemDataSource#update(Object, Object)} method will
     * be executed on every service thread update (for every key).
	 */
	public long getInterval();
	
	/**
	 * 
	 * @param interval Interval at which the update operation will be executed.
     * Effectively, the interval will be rounded to the nearest multiplicity of
     * the service update thread interval. The interval should be given in milliseconds.
     * If it is 0, the {@link CacheItemDataSource#update(Object, Object)} method will
     * be executed on every service thread update (for every key).
	 */
	public void setInterval(long interval);
	
	/**
	 * 
	 * @return Name of an mbean implementing the {@link RenewableCacheServiceMBean} interface, associated
	 * with this cache item.
	 */
	public String getMbeanName();
	
	/**
	 * 
	 * @return A unique id of this instance of cache item.
	 */
	public int getId();
	
	/**
	 * 
	 * @return Additional information about the state of a data source for this cache item. Returns
	 * {@link CacheItemDataSource#getInfo()}.
	 */
	public String getInfo();
	
	/**
	 * 
	 * @return Name of this {@link CacheItem} --- the fully qualified name of the class
	 * implementing {@link CacheItemDataSource}.
	 */
	public String getName();
    
	/**
	 * 
	 * @return A map of keys, which are handeled by this cache item, and corresponding last
	 * update times.
	 */
	public Map<K, Long> getKeysUpdates();
	
	/**
	 * 
	 * @return A set of keys, which are currently being updated.
	 */
	public Set<K> getKeysDuringUpdate();
	
	/**
	 * 
	 * @return A map of keys, in which an exception occured during an update.
	 */
	public Map<K, Throwable> getKeysExceptions();
}
