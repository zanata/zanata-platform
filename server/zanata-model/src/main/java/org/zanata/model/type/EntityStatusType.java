/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
import org.zanata.common.EntityStatus;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class EntityStatusType extends AbstractSingleColumnStandardBasicType<EntityStatus>
    implements DiscriminatorType<EntityStatus> {

    private static final long serialVersionUID = 9055584208831741141L;

    public EntityStatusType() {
        super(StringType.INSTANCE.getSqlTypeDescriptor(),
            EntityStatusTypeDescriptor.INSTANCE);
    }

    @Override
    public String toString(EntityStatus value) {
        return String.valueOf((value).getInitial());
    }

    @Override
    public String getName() {
        return "entityStatus";
    }

    @Override
    public String objectToSQLString(EntityStatus value, Dialect dialect)
            throws Exception {
        return "\'" + toString(value) + "\'";
    }

    public EntityStatus stringToObject(String xml) throws Exception {
        if (xml.length() != 1) {
            throw new MappingException(
                    "multiple or zero characters found parsing string");
        }
        return EntityStatus.valueOf(xml.charAt(0));
    }

    public EntityStatus fromStringValue(String xml) {
        return EntityStatus.valueOf(xml.charAt(0));
    }

}
