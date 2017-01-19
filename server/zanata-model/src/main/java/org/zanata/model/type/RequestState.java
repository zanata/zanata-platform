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

import org.hibernate.MappingException;

/**
 * Request state with single char
 *
 * Usage {@link org.zanata.model.Request}
 *
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public enum RequestState {
    NEW('N'),
    ACCEPTED('A'),
    REJECTED('R'),
    CANCELLED('C');
    private char initial;

    RequestState(char initial) {
        this.initial = initial;
    }

    public static RequestState getEnum(String string) {
        if (string.length() > 1) {
            throw new IllegalArgumentException(
                    "Invalid characters found parsing string \'" + string
                            + "\'");
        }
        char initial = string.charAt(0);
        switch (initial) {
        case 'N':
            return RequestState.NEW;

        case 'A':
            return RequestState.ACCEPTED;

        case 'R':
            return RequestState.REJECTED;

        case 'C':
            return RequestState.CANCELLED;

        default:
            throw new IllegalArgumentException(
                    "No request state has an initial matching " + initial);

        }
    }

    public char getInitial() {
        return this.initial;
    }
}
