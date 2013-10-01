/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.lock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Common behaviour for all locks in the system. Identifies locks in the system.
 * See {@link org.zanata.service.LockManagerService} for uses of locks.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class Lock implements Serializable {

    private final List<Object> properties;

    public Lock(Object... properties) {
        this.properties = Arrays.asList(properties);
    }

    public Lock add(Object prop) {
        this.properties.add(prop);
        return this;
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof Lock)) {
            return false;
        } else {
            Lock other = (Lock) obj;
            return properties.equals(other.properties);
        }
    }
}
