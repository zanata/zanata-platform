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
package org.zanata.model.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.ImmutableType;
import org.hibernate.type.LiteralType;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HCopyTransOptions.ConditionRuleAction;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
// FIXME convert to AbstractStandardBasicType approach
public class ConditionRuleActionType extends ImmutableType implements
        LiteralType<ConditionRuleAction> {
    @Override
    public String objectToSQLString(ConditionRuleAction value, Dialect dialect)
            throws Exception {
        return "'" + value.getInitial() + "'";
    }

    @Override
    public Object get(ResultSet rs, String name) throws HibernateException,
            SQLException {
        String dbVal = rs.getString(name);
        if (dbVal == null) {
            return null;
        } else {
            return HCopyTransOptions.ConditionRuleAction.valueOf(dbVal
                    .charAt(0));
        }
    }

    @Override
    public void set(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        st.setString(index, String
                .valueOf(((HCopyTransOptions.ConditionRuleAction) value)
                    .getInitial()));
    }

    @Override
    public int sqlType() {
        return Types.CHAR;
    }

    @Override
    public String toString(Object value) throws HibernateException {
        return String.valueOf(((HCopyTransOptions.ConditionRuleAction) value)
                .getInitial());
    }

    @Override
    public Object fromStringValue(String xml) throws HibernateException {
        return HCopyTransOptions.ConditionRuleAction.valueOf(xml.charAt(0));
    }

    @Override
    public Class<HCopyTransOptions.ConditionRuleAction> getReturnedClass() {
        return HCopyTransOptions.ConditionRuleAction.class;
    }

    @Override
    public String getName() {
        return "conditionRuleAction";
    }
}
