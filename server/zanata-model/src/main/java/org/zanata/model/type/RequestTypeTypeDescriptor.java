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
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class RequestTypeTypeDescriptor extends
        AbstractTypeDescriptor<RequestType> {

    public static final RequestTypeTypeDescriptor INSTANCE =
        new RequestTypeTypeDescriptor();

    public RequestTypeTypeDescriptor() {
        super(RequestType.class);
    }

    @Override
    public RequestType fromString(String string) {
        if (string == null) {
            return null;
        } else {
            return RequestType.getValueOf(string);
        }
    }

    @Override
    public String toString(RequestType value) {
        return value.getAbbr();
    }

    @Override
    public <X> X unwrap(RequestType value, Class<X> type,
            WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (String.class.isAssignableFrom(type)) {
            return (X) String.valueOf(value.getAbbr());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> RequestType wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (String.class.isInstance(value)) {
            return RequestType.getValueOf((String) value);
        }
        throw unknownWrap(value.getClass());
    }
}
