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
package org.zanata.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.zanata.model.type.ConditionRuleActionType;
import io.leangen.graphql.annotations.types.GraphQLType;

/**
 * Persistent representation of Copy Trans options.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@TypeDef(name = "conditionRuleAction",
        typeClass = ConditionRuleActionType.class)
@Cacheable
@GraphQLType(name = "CopyTransOptions")
public class HCopyTransOptions extends ModelEntityBase {

    private static final long serialVersionUID = -2604026393081629260L;

    /**
     * Indicates the different actions that can be taken when evaluating
     * conditions for a Text Flow during Copy Trans.
     */
    public enum ConditionRuleAction {

        /**
         * Indicates to reject the text flow.
         */
        REJECT,

        /**
         * Indicates to flag the potentially copied translation as fuzzy.
         */
        DOWNGRADE_TO_FUZZY,

        /**
         * Indicates to ignore the condition entirely.
         */
        IGNORE;

        public char getInitial() {
            return name().charAt(0);
        }

        public static ConditionRuleAction valueOf(char initial) {
            switch (initial) {
            case 'R':
                return REJECT;

            case 'D':
                return DOWNGRADE_TO_FUZZY;

            case 'I':
                return IGNORE;

            default:
                throw new IllegalArgumentException(String.valueOf(initial));

            }
        }
    }

    private ConditionRuleAction contextMismatchAction =
            ConditionRuleAction.REJECT;
    private ConditionRuleAction docIdMismatchAction =
            ConditionRuleAction.REJECT;
    private ConditionRuleAction projectMismatchAction =
            ConditionRuleAction.REJECT;

    @Type(type = "conditionRuleAction")
    @NotNull
    @Column(columnDefinition = "char(1)")
    public ConditionRuleAction getContextMismatchAction() {
        return contextMismatchAction;
    }

    @Type(type = "conditionRuleAction")
    @NotNull
    @Column(columnDefinition = "char(1)")
    public ConditionRuleAction getDocIdMismatchAction() {
        return docIdMismatchAction;
    }

    @Type(type = "conditionRuleAction")
    @NotNull
    @Column(columnDefinition = "char(1)")
    public ConditionRuleAction getProjectMismatchAction() {
        return projectMismatchAction;
    }

    public void setContextMismatchAction(
            final ConditionRuleAction contextMismatchAction) {
        this.contextMismatchAction = contextMismatchAction;
    }

    public void setDocIdMismatchAction(
            final ConditionRuleAction docIdMismatchAction) {
        this.docIdMismatchAction = docIdMismatchAction;
    }

    public void setProjectMismatchAction(
            final ConditionRuleAction projectMismatchAction) {
        this.projectMismatchAction = projectMismatchAction;
    }

    public HCopyTransOptions() {
    }

    @java.beans.ConstructorProperties({ "contextMismatchAction",
            "docIdMismatchAction", "projectMismatchAction" })
    public HCopyTransOptions(final ConditionRuleAction contextMismatchAction,
            final ConditionRuleAction docIdMismatchAction,
            final ConditionRuleAction projectMismatchAction) {
        this.contextMismatchAction = contextMismatchAction;
        this.docIdMismatchAction = docIdMismatchAction;
        this.projectMismatchAction = projectMismatchAction;
    }

    @Override
    public String toString() {
        return "HCopyTransOptions(contextMismatchAction="
                + this.getContextMismatchAction() + ", docIdMismatchAction="
                + this.getDocIdMismatchAction() + ", projectMismatchAction="
                + this.getProjectMismatchAction() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HCopyTransOptions that = (HCopyTransOptions) o;

        if (contextMismatchAction != that.contextMismatchAction) return false;
        if (docIdMismatchAction != that.docIdMismatchAction) return false;
        return projectMismatchAction == that.projectMismatchAction;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result +
                (contextMismatchAction != null ?
                        contextMismatchAction.hashCode() : 0);
        result = 31 * result +
                (docIdMismatchAction != null ? docIdMismatchAction.hashCode() :
                        0);
        result = 31 * result +
                (projectMismatchAction != null ?
                        projectMismatchAction.hashCode() : 0);
        return result;
    }
}
