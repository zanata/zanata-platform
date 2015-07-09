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

import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.StringType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class EntityTypeType extends AbstractSingleColumnStandardBasicType<EntityType>
    implements DiscriminatorType<EntityType> {

    public EntityTypeType() {
        super(StringType.INSTANCE.getSqlTypeDescriptor(),
            EntityTypeTypeDescriptor.INSTANCE);
    }

    @Override
    public String toString(EntityType value) {
        return String.valueOf((value).getAbbr());
    }

    @Override
    public String getName() {
        return "entityType";
    }

    @Override
    public String objectToSQLString(EntityType value, Dialect dialect)
        throws Exception {
        return "\'" + toString(value) + "\'";
    }

    public EntityType stringToObject(String xml) throws Exception {
        if (xml.length() < 1) {
            throw new MappingException(
                "multiple or zero characters found parsing string");
        }
        return EntityType.getValueOf(xml);
    }

    public EntityType fromStringValue(String xml) {
        return EntityType.getValueOf(xml);
    }
}
