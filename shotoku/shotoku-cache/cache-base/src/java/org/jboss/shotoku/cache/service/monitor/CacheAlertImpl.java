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

import java.text.DateFormat;
import java.util.Date;


/**
 * 
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class CacheAlertImpl implements CacheAlert {
	private long time;
	private String cause;
	private String description;
	private Object key;
	
	public CacheAlertImpl(String cause, String description, Object key) {
		super();
		this.time = System.currentTimeMillis();
		this.cause = cause;
		this.description = description;
		this.key = key;
	}
	
	public String getCause() {
		return cause;
	}
	
	public String getDescription() {
		return description;
	}
	
	public long getTime() {
		return time;
	}
	
	public Object getKey() {
		return key;
	}

	public String getTimeFormatted() {
		return DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(getTime()));
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((description == null) ? 0 : description.hashCode());
		result = PRIME * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CacheAlertImpl other = (CacheAlertImpl) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
}
