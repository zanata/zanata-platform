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

/**
 * Operations on a cache item that shouldn't be visible to the user --- mainly
 * the {@link #update()} method.
 * @param <K> Type of the keys in the cache.
 * @param <T> Type of the values bound to the keys in the cache.
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public interface CacheItemOperations<K,T> extends CacheItemUser<K,T> {
	/**
     * Called by the service update thread to update all keys and associated values.
     * You shouldn't call it from inside your code.
     */
    public void update();
	
	/**
	 * Reports that an update of a key ended wihtout any exceptions.
	 * @param key Key which has been updated.
	 */
	public void reportUpdateOk(K key);
	
	/**
	 * Reports that an update of a key ended with an exception.
	 * @param key Key which has been updated.
	 * @param t Exception which was thrown during the update.
	 */
	public void reportUpdateWithException(K key, Throwable t);
}
