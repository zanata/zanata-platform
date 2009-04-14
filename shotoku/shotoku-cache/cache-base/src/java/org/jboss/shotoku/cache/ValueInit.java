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
 * A class which represents either an initial value, which can be either a "dummy" one,
 * that is, a value that needs to be updated as soon as possible, or a real value, which
 * can be updated after a normal update interval.
 * Instances can be obtained using the
 * {@link ValueInit#realValue(Object)} and {@link ValueInit#dummyValue(Object)} methods.
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class ValueInit<T> {
	private boolean realValue;
	private T initValue;
	
	private ValueInit(boolean realValue, T initValue) {
		this.realValue = realValue;
		this.initValue = initValue;
	}
	
	public static <T> ValueInit<T> dummyValue(T value) {
		return new ValueInit<T>(false, value);
	}
	
	public static <T> ValueInit<T> realValue(T value) {
		return new ValueInit<T>(true, value);
	}
	
	public T getValue() {
		return initValue;
	}
	
	public boolean hasRealValue() {
		return realValue;
	}
}
