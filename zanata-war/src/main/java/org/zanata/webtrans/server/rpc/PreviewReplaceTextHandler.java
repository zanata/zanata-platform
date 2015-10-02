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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransUnitUpdatePreview;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.PreviewReplaceText;
import org.zanata.webtrans.shared.rpc.PreviewReplaceTextResult;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * Show the result of a {@link org.zanata.webtrans.shared.rpc.ReplaceText}
 * action without making any persistent changes.
 *
 * @author David Mason, damason@redhat.com
 *
 */
@Named("webtrans.gwt.PreviewReplaceTextHandler")
@javax.enterprise.context.Dependent
@ActionHandlerFor(PreviewReplaceText.class)
public class PreviewReplaceTextHandler extends
        AbstractActionHandler<PreviewReplaceText, PreviewReplaceTextResult> {
    @Inject
    private ZanataIdentity identity;

    @Override
    public PreviewReplaceTextResult execute(PreviewReplaceText previewAction,
            ExecutionContext context) throws ActionException {
        identity.checkLoggedIn();

        ReplaceTextHandler.replaceTextInUpdateRequests(previewAction
                .getAction());
        List<TransUnitUpdatePreview> previews =
                new ArrayList<TransUnitUpdatePreview>();
        for (TransUnitUpdateRequest request : previewAction.getAction()
                .getUpdateRequests()) {
            previews.add(new TransUnitUpdatePreview(request.getTransUnitId(),
                    request.getNewContents(), request.getNewContentState()));
        }
        return new PreviewReplaceTextResult(previews);
    }

    @Override
    public void rollback(PreviewReplaceText action,
            PreviewReplaceTextResult result, ExecutionContext context)
            throws ActionException {
        throw new ActionException("not supported");
    }
}
