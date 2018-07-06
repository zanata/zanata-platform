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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.zanata.common.HasContents;
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
    private static final String BACKEND_ID = "source";
    private UrlUtil urlUtil;

    @Inject
    public TextFlowsToMTDoc(UrlUtil urlUtil) {
        this.urlUtil = urlUtil;
    }

    @SuppressWarnings("unused")
    TextFlowsToMTDoc() {
    }

    public static String extractSingular(HasContents textFlow) {
        return textFlow.getContents().get(0);
    }

    // For messages with plural forms, the plural form will typically be more
    // general than singular. For example: singular: "One file was saved",
    // versus plural: "%d files were saved."
    // (but note that MT generally doesn't understand variables like %d)
    public static String extractPluralIfPresent(HasContents textFlow) {
        List<String> contents = textFlow.getContents();
        if (contents.size() > 1) {
            return contents.get(1);
        } else {
            return contents.get(0);
        }
    }

    public MTDocument fromTextFlows(String projectSlug,
            String versionSlug, String docId, LocaleId fromLocale,
            List<HTextFlow> textFlows,
            Function<HTextFlow, String> contentExtractor) {
        String url = buildDocUrl(projectSlug, versionSlug, docId);
        List<TypeString> contents =
                textFlows.stream()
                        .map(contentExtractor)
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
