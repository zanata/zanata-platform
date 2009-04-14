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

import org.jboss.shotoku.cache.CacheItem;

/**
 * Information about probably incorrect behaviour of cache items, collected
 * by the monitoring thread.
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public interface CacheAlert {
	/**
	 * 
	 * @return Time on which the represented behaviour was observed.
	 */
	public long getTime();
	/**
	 * 
	 * @return Time on which the represented behaviour was observed, formatted as a string.
	 */
	public String getTimeFormatted();
	/**
	 * 
	 * @return Short description of the incorrect behaviour.
	 */
	public String getDescription();
	/**
	 * 
	 * @return Possible/definite cause of the incorrect behaviour.
	 */
	public String getCause();
	/**
	 * 
	 * @return {@link CacheItem} key, for which the incorrect behaviour occured.
	 */
	public Object getKey();
}
