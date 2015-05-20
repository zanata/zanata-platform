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

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.StringType;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HCopyTransOptions.ConditionRuleAction;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class ConditionRuleActionType extends
    AbstractSingleColumnStandardBasicType<ConditionRuleAction> implements
        LiteralType<ConditionRuleAction> {

    public ConditionRuleActionType() {
        super(StringType.INSTANCE.getSqlTypeDescriptor(),
            ConditionRuleActionTypeDescriptor.INSTANCE);
    }


    @Override
    public String objectToSQLString(ConditionRuleAction value, Dialect dialect)
            throws Exception {
        return "\'" + toString(value) + "\'";
    }

    @Override
    public String toString(HCopyTransOptions.ConditionRuleAction value) throws HibernateException {
        return String.valueOf((value).getInitial());
    }

    @Override
    public HCopyTransOptions.ConditionRuleAction fromStringValue(String xml) throws HibernateException {
        return HCopyTransOptions.ConditionRuleAction.valueOf(xml.charAt(0));
    }

    @Override
    public String getName() {
        return "conditionRuleAction";
    }
}
