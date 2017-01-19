/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.List;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.service.TransMemoryMergeService;
import org.zanata.service.TranslationService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

@Named("webtrans.gwt.TransMemoryMergeHandler")
@RequestScoped
@ActionHandlerFor(TransMemoryMerge.class)
public class TransMemoryMergeHandler
        extends AbstractActionHandler<TransMemoryMerge, UpdateTransUnitResult> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TransMemoryMergeHandler.class);

    @Inject
    private TransMemoryMergeService transMemoryMergeServiceImpl;
    @Inject
    private TransUnitUpdateHelper transUnitUpdateHelper;

    @Override
    public UpdateTransUnitResult execute(TransMemoryMerge action,
            ExecutionContext context) throws ActionException {
        List<TranslationService.TranslationResult> translationResults =
                transMemoryMergeServiceImpl.executeMerge(action);
        return transUnitUpdateHelper
                .generateUpdateTransUnitResult(translationResults);
    }

    @Override
    public void rollback(TransMemoryMerge action, UpdateTransUnitResult result,
            ExecutionContext context) throws ActionException {
    }
}
