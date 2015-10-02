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

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationMemoryService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;

@Named("webtrans.gwt.GetTransMemoryHandler")
@javax.enterprise.context.Dependent
@ActionHandlerFor(GetTranslationMemory.class)
@Slf4j
public class GetTransMemoryHandler extends
        AbstractActionHandler<GetTranslationMemory, GetTranslationMemoryResult> {

    @Inject
    private ZanataIdentity identity;

    @Inject
    private TranslationMemoryService translationMemoryServiceImpl;

    @Override
    public GetTranslationMemoryResult execute(GetTranslationMemory action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();

        TransMemoryQuery transMemoryQuery = action.getQuery();
        log.debug("Fetching matches for {}", transMemoryQuery);

        List<TransMemoryResultItem> results =
                translationMemoryServiceImpl.searchTransMemory(
                        action.getLocaleId(), action.getSourceLocaleId(),
                        transMemoryQuery);

        log.debug("Returning {} TM matches for {}", results.size(),
                transMemoryQuery);
        return new GetTranslationMemoryResult(action, results);
    }

    @Override
    public void rollback(GetTranslationMemory action,
            GetTranslationMemoryResult result, ExecutionContext context)
            throws ActionException {
    }
}
