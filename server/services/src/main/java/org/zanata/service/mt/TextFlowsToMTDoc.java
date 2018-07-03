/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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
package org.zanata.service.mt;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.zanata.common.LocaleId;
import org.zanata.model.HTextFlow;
import org.zanata.service.mt.dto.MTDocument;
import org.zanata.service.mt.dto.TypeString;
import org.zanata.util.UrlUtil;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Dependent
public class TextFlowsToMTDoc {
    protected static final String BACKEND_ID = "dev";
    private UrlUtil urlUtil;

    @Inject
    public TextFlowsToMTDoc(UrlUtil urlUtil) {
        this.urlUtil = urlUtil;
    }

    public TextFlowsToMTDoc() {
    }

    public MTDocument fromTextFlows(String projectSlug, String versionSlug, String docId, LocaleId fromLocale, List<HTextFlow> textFlows) {
        String url = buildDocUrl(projectSlug, versionSlug, docId);
        List<TypeString> contents =
                textFlows.stream()
                        // TODO we can't support plural yet
                        .map(textFlow -> textFlow.getContents().get(0))
                        .map(TypeString::new)
                        .collect(Collectors.toList());

        return new MTDocument(url, contents, fromLocale.getId(), BACKEND_ID);
    }

    /**
     *
     * @return URL that complies with our SourceDocResource API.
     */
    private String buildDocUrl(String projectSlug, String versionSlug,
            String docId) {
        return urlUtil.restPath("project/p/" + projectSlug +"/iterations/i" + versionSlug + "/resource?docId=" + docId);
    }

    public MTDocument fromSingleTextFlow(String projectSlug, String versionSlug, String docId, LocaleId fromLocale, HTextFlow textFlow) {
        String url = buildDocUrl(projectSlug, versionSlug, docId);
        List<TypeString> contents = textFlow.getContents()
                .stream().map(TypeString::new).collect(Collectors.toList());

        return new MTDocument(url, contents, fromLocale.getId(), BACKEND_ID);
    }
}
