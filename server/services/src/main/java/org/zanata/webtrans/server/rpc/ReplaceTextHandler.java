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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.service.SecurityService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.ReplaceText;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;
import com.google.common.base.Strings;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

@Named("webtrans.gwt.ReplaceTextHandler")
@RequestScoped
@ActionHandlerFor(ReplaceText.class)
public class ReplaceTextHandler
        extends AbstractActionHandler<ReplaceText, UpdateTransUnitResult> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ReplaceTextHandler.class);

    @Inject
    @Any
    UpdateTransUnitHandler updateTransUnitHandler;
    @Inject
    SecurityService securityServiceImpl;

    @Override
    public UpdateTransUnitResult execute(ReplaceText action,
            ExecutionContext context) throws ActionException {
        securityServiceImpl.checkWorkspaceAction(action.getWorkspaceId(),
                SecurityService.TranslationAction.MODIFY);
        replaceTextInUpdateRequests(action);
        return updateTransUnitHandler.execute(action, context);
    }

    /**
     * Replaces occurrences of a search string with a replacement string in all
     * content strings in action.getUpdateRequests()
     *
     * @param action
     *            action containing update requests that will be modified
     * @throws ActionException
     *             if searchText or replaceText in action are null or empty
     */
    public static void replaceTextInUpdateRequests(ReplaceText action)
            throws ActionException {
        String searchText = action.getSearchText();
        String replaceText = action.getReplaceText();
        if (Strings.isNullOrEmpty(searchText)
                || Strings.isNullOrEmpty(replaceText)) {
            throw new ActionException("search or replace text is empty");
        }
        int flags = action.isCaseSensitive() ? Pattern.UNICODE_CASE
                : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        Pattern pattern = Pattern.compile(Pattern.quote(searchText), flags);
        for (TransUnitUpdateRequest request : action.getUpdateRequests()) {
            List<String> contents = request.getNewContents();
            log.debug("transUnit {} before replace [{}]",
                    request.getTransUnitId(), contents);
            for (int i = 0; i < contents.size(); i++) {
                String content = contents.get(i);
                Matcher matcher = pattern.matcher(content);
                String newContent = matcher
                        .replaceAll(Matcher.quoteReplacement(replaceText));
                contents.set(i, newContent);
            }
            log.debug("transUnit {} after replace [{}]",
                    request.getTransUnitId(), contents);
        }
    }

    @Override
    public void rollback(ReplaceText action, UpdateTransUnitResult result,
            ExecutionContext context) throws ActionException {
        throw new ActionException("not supported");
    }
}
