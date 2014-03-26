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
import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.model.HCopyTransOptions;
import org.zanata.util.ZanataMessages;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Holds a {@link org.zanata.model.HCopyTransOptions} model object. This
 * component is intended for use within other components that need to keep a
 * copy of a CopyTransOptions entity, although it may be accessed directly as
 * well.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("copyTransOptionsModel")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class CopyTransOptionsModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private EntityManager entityManager;

    @Setter
    private HCopyTransOptions instance;

    @In
    private ZanataMessages zanataMessages;

    public HCopyTransOptions getInstance() {
        if (instance == null) {
            instance = new HCopyTransOptions();
        }
        return instance;
    }

    public String getProjectMismatchAction() {
        return getInstance().getProjectMismatchAction().toString();
    }

    private void setProjectMismatchAction(String projectMismatchAction) {
        getInstance().setProjectMismatchAction(
                HCopyTransOptions.ConditionRuleAction
                        .valueOf(projectMismatchAction));
    }

    public String getDocIdMismatchAction() {
        return getInstance().getDocIdMismatchAction().toString();
    }

    private void setDocIdMismatchAction(String docIdMismatchAction) {
        getInstance().setDocIdMismatchAction(
                HCopyTransOptions.ConditionRuleAction
                        .valueOf(docIdMismatchAction));
    }

    public String getContextMismatchAction() {
        return getInstance().getContextMismatchAction().toString();
    }

    private void setContextMismatchAction(String contextMismatchAction) {
        getInstance().setContextMismatchAction(
                HCopyTransOptions.ConditionRuleAction
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

    @CachedMethodResult
    public List<RuleAction> getRuleActions() {
        List<RuleAction> list = Lists.newArrayList();
        list.add(new RuleAction(HCopyTransOptions.ConditionRuleAction.IGNORE,
                "button--success", zanataMessages
                        .getMessage("jsf.iteration.CopyTrans.Action.continue")));

        list.add(new RuleAction(
                HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY,
                "button--unsure",
                zanataMessages
                        .getMessage("jsf.iteration.CopyTrans.Action.downgradeToFuzzy")));

        list.add(new RuleAction(HCopyTransOptions.ConditionRuleAction.REJECT,
                "button--danger", zanataMessages
                        .getMessage("jsf.iteration.CopyTrans.Action.reject")));
        return list;
    }

    public void save() {
        this.setInstance(entityManager.merge(this.getInstance()));
    }

    @AllArgsConstructor
    @Getter
    public class RuleAction {
        private HCopyTransOptions.ConditionRuleAction action;
        private String cssClass;
        private String displayText;
    }
}
