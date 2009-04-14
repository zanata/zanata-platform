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

import org.jboss.shotoku.cache.service.RenewableCacheServiceMBean;

/**
 * Basic interface when using a renewable cache item --- defines methods, which
 * are used to determine what values should be put to the cache. The {@link #init(Object)} method
 * is called when a user demands a value for a key that hasn't been accessed before.
 * That value is then placed in the cache, and updated periodically in the background,
 * using the {@link #update(Object, Object)} method. How often the updates happen,
 * depends on the settings of the {@link RenewableCacheServiceMBean} and on
 * settings passed when creating the cache item
 * ({@link CacheItem#create(CacheItemDataSource, org.jboss.cache.Fqn, String, long)}).
 * @param <K> Type of keys in the cache. The keys
 * should bahave well as map keys (most probably, the hashCode() and
 * equals() methods should be overriden). Moreover, they cannot be null.
 * @param <T> Type of the values bound to the keys in the cache.
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public interface CacheItemDataSource<K, T> {
	/**
     * Called by the service periodically to update the object held in the
     * cache, bound to the given key. The value current held in the cache can be changed or not.
     * @param key Key of the object to update. Cannot be null.
     * @param currentObject Current value held in the cache.
     * @return Can return either:
     * <ul>
     * <li>{@link ValueChange#noChange()} to indicate that the value currently held in the cache
     * shouldn't be changed.</li>
     * <li>{@link ValueChange#changeTo(Object)} to bind a new value to the given key in
     * the cache.</li>
     * <li>{@link ValueChange#changeToReleaseOld(Object, Releasable)} to bind a ne value to the given
     * key in the cache, and then release the value.</li>
     * </ul>
     */
    public ValueChange<? extends T> update(K key, T currentObject);

    /**
     * Called when the user demands an object which hasn't been accessed
     * before, and thus, which hasn't been yet initialized.
     * @param key Key of the object to initialize. Cannot be null.
     * @return Can return either:
     * <ul>
     * <li>{@link ValueInit#realValue(Object)} to indicate that the value returned is a "real" one;
     * it will be updated after this cache item's interval.</li>
     * <li>{@link ValueInit#dummyValue(Object)} to indicate that the value returned is a "dummy" one,
     * and that it should be updated as soon as possible, that is, on the nearest cache service update.
     * This is only useful if the cache item interval is significantly larger then the service interval.
     * A specific use case is when it is not acceptable to construct a new value, that will be held in
     * the cache, as a blocking operation, but instead return a new "dummy" value which will be quickly
     * replaced by a real one, by an update in the background.
     * </ul>
     */
    public ValueInit<? extends T> init(K key);
    
    /**
     * 
     * @return Additional information about this data source, which is visible for example
     * in the cache administration web application. Has no influence on the behaviour or
     * content of the cache.
     */
    public String getInfo();
}
