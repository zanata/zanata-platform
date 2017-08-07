/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.rest.editor.service;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HTextFlow;
import org.zanata.rest.editor.dto.EditorTextFlow;
import org.zanata.rest.editor.dto.TransUnit;
import org.zanata.rest.editor.dto.TransUnits;
import org.zanata.rest.editor.service.resource.TextFlowResource;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
@Named("editor.textFlowService")
@Path(TextFlowResource.SERVICE_PATH)
@Transactional(readOnly = true)
public class TextFlowService implements TextFlowResource {
    @Inject
    private TextFlowDAO textFlowDAO;
    @Inject
    private TransUnitUtils transUnitUtils;

    @Override
    public Response get(@QueryParam("ids") String ids) {
        TransUnits transUnits = new TransUnits();
        if (StringUtils.isEmpty(ids)) {
            return Response.ok(transUnits).build();
        }
        List<Long> idList = TransUnitUtils.filterAndConvertIdsToList(ids);
        if (idList.size() > TransUnitUtils.MAX_SIZE) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        List<HTextFlow> hTextFlows = textFlowDAO.findByIdList(idList);
        for (HTextFlow htf : hTextFlows) {
            LocaleId localeId = htf.getDocument().getLocale().getLocaleId();
            EditorTextFlow tf = new EditorTextFlow(htf.getResId(), localeId);
            transUnitUtils.transferToTextFlow(htf, tf);
            transUnits.put(htf.getId().toString(), new TransUnit(tf));
        }
        return Response.ok(transUnits).build();
    }

    public TextFlowService() {
    }

    @java.beans.ConstructorProperties({ "textFlowDAO", "transUnitUtils" })
    protected TextFlowService(final TextFlowDAO textFlowDAO,
            final TransUnitUtils transUnitUtils) {
        this.textFlowDAO = textFlowDAO;
        this.transUnitUtils = transUnitUtils;
    }
}
