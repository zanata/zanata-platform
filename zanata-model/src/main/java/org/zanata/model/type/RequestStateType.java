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
import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.StringType;

public class RequestStateType extends AbstractSingleColumnStandardBasicType<RequestState>
    implements DiscriminatorType<RequestState> {

    public RequestStateType() {
        super(StringType.INSTANCE.getSqlTypeDescriptor(),
            RequestStateTypeDescriptor.INSTANCE);
    }

    @Override
    public String toString(RequestState value) {
        return String.valueOf((value).getInitial());
    }

    @Override
    public String getName() {
        return "requestState";
    }

    @Override
    public String objectToSQLString(RequestState value, Dialect dialect)
            throws Exception {
        return "\'" + toString(value) + "\'";
    }

    public RequestState stringToObject(String xml) throws Exception {
        if (xml.length() != 1) {
            throw new MappingException(
                    "multiple or zero characters found parsing string '" + xml
                            + "'");
        }
        return RequestState.getEnum(xml);
    }

    public RequestState fromStringValue(String xml) {
        assert xml.length() > 0;
        return RequestState.getEnum(xml);
    }

}
