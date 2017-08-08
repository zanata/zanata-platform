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

import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import net.customware.gwt.dispatch.shared.ActionException;
import org.zanata.exception.ZanataServiceException;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GlossarySearchService;
import org.zanata.util.ShortString;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;
import net.customware.gwt.dispatch.server.ExecutionContext;


@Named("webtrans.gwt.GetGlossaryHandler")
@RequestScoped
@ActionHandlerFor(GetGlossary.class)
public class GetGlossaryHandler
        extends AbstractActionHandler<GetGlossary, GetGlossaryResult> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GetGlossaryHandler.class);

    // X2 in total: Global glossary and Project glossary
    private static final int MAX_RESULTS = 20;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private GlossarySearchService glossarySearchService;

    @Override
    public GetGlossaryResult execute(GetGlossary action,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();
        String searchText = action.getQuery();
        ShortString abbrev = new ShortString(searchText);
        SearchType searchType = action.getSearchType();
        log.debug("Fetching Glossary matches({}) for \"{}\"", searchType,
                abbrev);

        ArrayList<GlossaryResultItem> results;
        try {
            results = glossarySearchService.searchGlossary(
                    action.getSrcLocaleId(),
                    action.getLocaleId(),
                    searchText,
                    searchType,
                    MAX_RESULTS,
                    action.getProjectIterationId().getProjectSlug());
        } catch (ZanataServiceException e) {
            throw new ActionException(e.getMessage(), e);
        }

        log.debug("Returning {} Glossary matches for \"{}\"", results.size(),
                abbrev);
        return new GetGlossaryResult(action, results);
    }

    @Override
    public void rollback(GetGlossary action, GetGlossaryResult result,
            ExecutionContext context) throws ActionException {
    }
}
