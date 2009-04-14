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
 * Function that should be executed by an update thread; updates an associated
 * key in a cache item.
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public abstract class UpdateThreadData<K, T> {
	private long createTime;
	
	private K key;
	private CacheItemOperations<K, T> cacheItem;
	
    public UpdateThreadData(K key, CacheItemOperations<K, T> cacheItem) {
		createTime = System.currentTimeMillis();
		
		this.key = key;
		this.cacheItem = cacheItem;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void executeOk() {
		cacheItem.reportUpdateOk(key);
	}
	
	public void executeWithException(Throwable t) {
		cacheItem.reportUpdateWithException(key, t);
	}
	
	public abstract void execute();
}
