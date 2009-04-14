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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * 
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class CacheAlertFactory {
	public static CacheAlert createAlertKeyNotUpdated(Object key) {
		return new CacheAlertImpl(null, "Key hasn't been updated for a long time.", key);
	}
	
	public static CacheAlert createAlertKeyTooLongInUpdate(Object key) {
		return new CacheAlertImpl(null, "Key is too long in update.", key);
	}
	
	public static CacheAlert createAlertKeyException(Object key, Throwable t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter target = new PrintWriter(baos);
		t.printStackTrace(target);
		target.flush();
		return new CacheAlertImpl(baos.toString(), "An exception occured during key update.", key);
	}
}
