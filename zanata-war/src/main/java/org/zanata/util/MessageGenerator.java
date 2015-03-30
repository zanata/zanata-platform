/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.util;

import org.apache.commons.lang.StringUtils;
import org.zanata.model.HDocument;
import org.zanata.model.HTextFlowTarget;

/**
 * Generate messages/comments of Zanata business actions
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MessageGenerator {

    /**
     * Prefix of action type for generated message
     */
    public static final String PREFIX_MERGE_TRANS = "Merge translations";
    public static final String PREFIX_COPY_TRANS = "Copy translation";
    public static final String PREFIX_COPY_VERSION = "Copy version";

    /**
     * Create revision comment for translation that is copied by merge
     * translation
     *
     * @see org.zanata.service.MergeTranslationsService
     *
     * @param tft - HTextFlowTarget to copy from
     */
    public static final String getMergeTranslationMessage(
            HTextFlowTarget tft) {
        HDocument document = tft.getTextFlow().getDocument();
        String author = "";
        if (tft.getLastModifiedBy() != null) {
            author = tft.getLastModifiedBy().getName();
        }

        return generateAutoCopiedMessage(PREFIX_MERGE_TRANS,
                document.getProjectIteration().getProject().getName(),
                document.getProjectIteration()
                        .getSlug(), document.getDocId(), author);
    }

    /**
     * Create revision comment for translation that is copied by copy trans
     * @see org.zanata.service.CopyTransService
     *
     * @param tft - HTextFlowTarget to copy from
     */
    public static final String getCopyTransMessage(HTextFlowTarget tft) {
        HDocument document = tft.getTextFlow().getDocument();
        String author = "";
        if (tft.getLastModifiedBy() != null) {
            author = tft.getLastModifiedBy().getName();
        }
        return generateAutoCopiedMessage(PREFIX_COPY_TRANS,
                document.getProjectIteration()
                        .getProject().getName(), document.getProjectIteration()
                        .getSlug(), document.getDocId(), author);
    }

    /**
     * Create revision comment for translation that is copied by copy version
     * @see org.zanata.service.CopyVersionService
     *
     * @param tft - HTextFlowTarget to copy from
     */
    public static final String getCopyVersionMessage(HTextFlowTarget tft) {
        HDocument document = tft.getTextFlow().getDocument();
        String author = "";
        if (tft.getLastModifiedBy() != null) {
            author = tft.getLastModifiedBy().getName();
        }
        return generateAutoCopiedMessage(PREFIX_COPY_VERSION,
                document.getProjectIteration().getProject().getName(),
                document.getProjectIteration()
                        .getSlug(), document.getDocId(), author);
    }

    private static final String generateAutoCopiedMessage(String prefix,
        String projectName, String versionSlug, String docId, String author) {
        StringBuilder comment = new StringBuilder();

        comment.append(prefix + ": translation auto-copied from project ")
                .append(projectName)
                .append(", version ")
                .append(versionSlug)
                .append(", document ")
                .append(docId);

        if (!StringUtils.isEmpty(author)) {
            comment.append(", author ")
                    .append(author);
        }
        return comment.toString();
    }
}
