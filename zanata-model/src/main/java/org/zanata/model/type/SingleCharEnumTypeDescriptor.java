/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.model.type;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class SingleCharEnumTypeDescriptor<T extends Enum> extends AbstractTypeDescriptor<T> {

    protected SingleCharEnumTypeDescriptor(Class<T> type) {
        super(type);
    }

    @Override
    public String toString(T value) {
        return String.valueOf(getIdentifyingChar(value));
    }

    @Override
    public T fromString(String string) {
        if (string == null) {
            return null;
        } else {
            return valueOf(string);
        }
    }

    @Override
    public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (String.class.isAssignableFrom(type)) {
            return (X) String.valueOf(getIdentifyingChar(value));
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> T wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (String.class.isInstance(value)) {
            return valueOf(((String) value));
        }
        throw unknownWrap(value.getClass());
    }

    /**
     * @param value
     * @return initial of the type value
     */
    abstract char getIdentifyingChar(T value);

    /**
     * @param string
     * @return Type T from given string
     */
    abstract T valueOf(String string);
}
