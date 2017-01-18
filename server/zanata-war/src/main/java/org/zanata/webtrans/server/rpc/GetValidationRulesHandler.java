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
package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.rpc.GetValidationRulesAction;
import org.zanata.webtrans.shared.rpc.GetValidationRulesResult;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Named("getValidationRulesHandler")
@ActionHandlerFor(GetValidationRulesAction.class)
@RequestScoped
public class GetValidationRulesHandler
        extends
        AbstractActionHandler<GetValidationRulesAction, GetValidationRulesResult> {
    @Inject
    private ValidationService validationServiceImpl;

    @Override
    public GetValidationRulesResult execute(GetValidationRulesAction action,
            ExecutionContext context) throws ActionException {
        ProjectIterationId version =
                action.getWorkspaceId().getProjectIterationId();
        Collection<ValidationAction> validationActionList =
                validationServiceImpl.getValidationActions(
                        version.getProjectSlug(), version.getIterationSlug());

        HashMap<ValidationId, State> result =
                new HashMap<ValidationId, State>();

        for (ValidationAction validationAction : validationActionList) {
            result.put(validationAction.getId(), validationAction.getState());
        }

        return new GetValidationRulesResult(result);
    }

    @Override
    public void rollback(GetValidationRulesAction action,
            GetValidationRulesResult result, ExecutionContext context)
            throws ActionException {
    }
}
