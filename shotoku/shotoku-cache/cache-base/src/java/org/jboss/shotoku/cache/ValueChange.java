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
 * A class which represents either a change of value, and holds the new value,
 * or no value change. Instances can be obtained using the
 * {@link ValueChange#noChange()}, {@link ValueChange#changeTo(Object)} and
 * {@link ValueChange#changeToReleaseOld(Object, Releasable)} methods.
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class ValueChange<T> {
	private boolean hasValue;
	private T newValue;
	private Releasable release;
	
	private ValueChange(boolean hasValue, T newValue, Releasable release) {
		this.hasValue = hasValue;
		this.newValue = newValue;
		this.release = release;
	}
	
	public static <T> ValueChange<T> noChange() {
		return new ValueChange<T>(false, null, null);
	}
	
	public static <T> ValueChange<T> changeTo(T newValue) {
		return new ValueChange<T>(true, newValue, null);
	}
	
	public static <T> ValueChange<T> changeToReleaseOld(T newValue, Releasable oldValue) {
		return new ValueChange<T>(true, newValue, oldValue);
	}
	
	public T getValue() {
		return newValue;
	}
	
	public boolean hasValue() {
		return hasValue;
	}
	
	public Releasable getRelease() {
		return release;
	}
}
