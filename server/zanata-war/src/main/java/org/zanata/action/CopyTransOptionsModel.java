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
package org.zanata.action;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.persistence.EntityManager;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.i18n.Messages;
import org.zanata.model.HCopyTransOptions;
import org.zanata.service.impl.CopyTransOptionFactory;
import com.google.common.collect.Lists;

/**
 * Holds a {@link org.zanata.model.HCopyTransOptions} model object. This
 * component is intended for use within other components that need to keep a
 * copy of a CopyTransOptions entity, although it may be accessed directly as
 * well.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("copyTransOptionsModel")
@ViewScoped
@Model
@Transactional
public class CopyTransOptionsModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    private EntityManager entityManager;
    private HCopyTransOptions instance;
    @Inject
    private Messages msgs;
    private final java.util.concurrent.atomic.AtomicReference<Object> ruleActions =
            new java.util.concurrent.atomic.AtomicReference<Object>();

    public HCopyTransOptions getInstance() {
        if (instance == null) {
            instance = CopyTransOptionFactory.getImplicitOptions();
        }
        return instance;
    }

    public String getProjectMismatchAction() {
        return getInstance().getProjectMismatchAction().toString();
    }

    private void setProjectMismatchAction(String projectMismatchAction) {
        getInstance()
                .setProjectMismatchAction(HCopyTransOptions.ConditionRuleAction
                        .valueOf(projectMismatchAction));
    }

    public String getDocIdMismatchAction() {
        return getInstance().getDocIdMismatchAction().toString();
    }

    private void setDocIdMismatchAction(String docIdMismatchAction) {
        getInstance()
                .setDocIdMismatchAction(HCopyTransOptions.ConditionRuleAction
                        .valueOf(docIdMismatchAction));
    }

    public String getContextMismatchAction() {
        return getInstance().getContextMismatchAction().toString();
    }

    private void setContextMismatchAction(String contextMismatchAction) {
        getInstance()
                .setContextMismatchAction(HCopyTransOptions.ConditionRuleAction
                        .valueOf(contextMismatchAction));
    }

    public void update(String action, String value) {
        if (action.equalsIgnoreCase("ContextMismatch")) {
            setContextMismatchAction(value);
        } else if (action.equalsIgnoreCase("ProjectMismatch")) {
            setProjectMismatchAction(value);
        } else if (action.equalsIgnoreCase("DocIdMismatch")) {
            setDocIdMismatchAction(value);
        }
    }

    private List<RuleAction> getRuleActionsList() {
        return Lists.newArrayList(
                new RuleAction(HCopyTransOptions.ConditionRuleAction.IGNORE,
                        "button--success",
                        msgs.get("jsf.iteration.CopyTrans.Action.continue")),
                new RuleAction(
                        HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY,
                        "button--unsure",
                        msgs.get(
                                "jsf.iteration.CopyTrans.Action.downgradeToFuzzy")),
                new RuleAction(HCopyTransOptions.ConditionRuleAction.REJECT,
                        "button--danger",
                        msgs.get("jsf.iteration.CopyTrans.Action.reject")));
    }

    public void save() {
        this.setInstance(entityManager.merge(this.getInstance()));
    }

    public class RuleAction {
        private HCopyTransOptions.ConditionRuleAction action;
        private String cssClass;
        private String displayText;

        @java.beans.ConstructorProperties({ "action", "cssClass",
                "displayText" })
        public RuleAction(final HCopyTransOptions.ConditionRuleAction action,
                final String cssClass, final String displayText) {
            this.action = action;
            this.cssClass = cssClass;
            this.displayText = displayText;
        }

        public HCopyTransOptions.ConditionRuleAction getAction() {
            return this.action;
        }

        public String getCssClass() {
            return this.cssClass;
        }

        public String getDisplayText() {
            return this.displayText;
        }
    }

    public void setInstance(final HCopyTransOptions instance) {
        this.instance = instance;
    }

    public List<RuleAction> getRuleActions() {
        Object value = this.ruleActions.get();
        if (value == null) {
            synchronized (this.ruleActions) {
                value = this.ruleActions.get();
                if (value == null) {
                    final List<RuleAction> actualValue = getRuleActionsList();
                    value = actualValue == null ? this.ruleActions
                            : actualValue;
                    this.ruleActions.set(value);
                }
            }
        }
        return (List<RuleAction>) (value == this.ruleActions ? null : value);
    }
}
