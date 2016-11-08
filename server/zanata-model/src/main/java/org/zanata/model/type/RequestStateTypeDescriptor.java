/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.model.type;

/**
 * Convert between a single character and a RequestState, used by
 * {@link RequestStateType}.
 */
public class RequestStateTypeDescriptor
        extends SingleCharEnumTypeDescriptor<RequestState> {

    public static final RequestStateTypeDescriptor INSTANCE =
            new RequestStateTypeDescriptor();

    protected RequestStateTypeDescriptor() {
        super(RequestState.class);
    }

    @Override
    char getIdentifyingChar(RequestState value) {
        return value.getInitial();
    }

    @Override
    RequestState valueOf(String string) {
        if (string.length() == 1) {
            return RequestState.getEnum(string);
        }
        throw new IllegalArgumentException(
                "String to look up a RequestState must be exactly 1 character. "
                        + "Received \"" + string + "\"");
    }
}
