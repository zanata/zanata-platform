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

import org.apache.commons.lang3.StringUtils;
import org.zanata.model.HDocument;
import org.zanata.model.HPerson;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import org.zanata.model.ITextFlowTargetHistory;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.type.EntityType;
import org.zanata.webtrans.shared.model.TransMemoryDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslationUtil {
    /**
     * Prefix of action type for generated message
     */
    public static final String PREFIX_MERGE_VERSION = "Merge version";
    public static final String PREFIX_COPY_TRANS = "Copy translation";
    public static final String PREFIX_COPY_VERSION = "Copy version";
    public static final String PREFIX_TM_MERGE = "TM Merge";

    /**
     * Copy copiedEntityId and entityType from copyFrom to copyTo
     *
     * copyFrom.id will be used if copyFrom.copiedEntityId = null
     * {@link org.zanata.model.type.EntityType#HTexFlowTarget} be used if
     * copyFrom.copiedEntityType = null
     *
     *
     * @param copyFrom
     * @param copyTo
     */
    public static void copyEntity(@Nonnull ITextFlowTargetHistory copyFrom,
        @Nonnull ITextFlowTargetHistory copyTo) {
        copyTo.setCopiedEntityType(getCopiedEntityType(copyFrom));
        copyTo.setCopiedEntityId(getCopiedEntityId(copyFrom));
    }

    /**
     * Create revision comment for translation that is copied by merge
     * translation
     *
     * @see org.zanata.service.MergeTranslationsService
     *
     * @param tft - HTextFlowTarget to copy from
     */
    public static String getMergeTranslationMessage(
        HTextFlowTarget tft) {
        HDocument document = tft.getTextFlow().getDocument();
        return generateAutoCopiedMessage(PREFIX_MERGE_VERSION,
            document.getProjectIteration().getProject().getName(),
            document.getProjectIteration()
                .getSlug(), document.getDocId(),
            getAuthor(tft.getLastModifiedBy()));
    }

    /**
     * Create revision comment for translation that is copied by copy trans
     * @see org.zanata.service.CopyTransService
     *
     * @param tft - HTextFlowTarget to copy from
     */
    public static String getCopyTransMessage(HTextFlowTarget tft) {
        HDocument document = tft.getTextFlow().getDocument();
        return generateAutoCopiedMessage(PREFIX_COPY_TRANS,
            document.getProjectIteration()
                .getProject().getName(), document.getProjectIteration()
                .getSlug(), document.getDocId(),
            getAuthor(tft.getLastModifiedBy()));
    }

    /**
     * Create revision comment for translation that is copied by copy version
     * @see org.zanata.service.CopyVersionService
     *
     * @param tft - HTextFlowTarget to copy from
     */
    public static String getCopyVersionMessage(HTextFlowTarget tft) {
        HDocument document = tft.getTextFlow().getDocument();
        return generateAutoCopiedMessage(PREFIX_COPY_VERSION,
            document.getProjectIteration().getProject().getName(),
            document.getProjectIteration()
                .getSlug(), document.getDocId(),
            getAuthor(tft.getLastModifiedBy()));
    }

    /**
     * Create revision comment for translation that is copied by TM Merge
     * @see org.zanata.service.TransMemoryMergeService
     *
     * @param tu
     */
    public static String getTMMergeMessage(TransMemoryUnit tu) {
        return PREFIX_TM_MERGE +
                ": translation copied from translation memory '" +
                tu.getTranslationMemory().getSlug() +
                "', description '" +
                tu.getTranslationMemory().getDescription() +
                "'";
    }

    /**
     * Create revision comment for translation that is copied by TM Merge
     * @see org.zanata.service.TransMemoryMergeService
     *
     * @param tmDetails
     */
    public static String getTMMergeMessage(TransMemoryDetails tmDetails) {
        return generateAutoCopiedMessage(PREFIX_TM_MERGE,
            tmDetails.getProjectName(),
            tmDetails.getIterationName(), tmDetails.getDocId(),
            tmDetails.getLastModifiedBy());
    }

    private static String getAuthor(HPerson person) {
        if (person != null) {
            return person.getName();
        }
        return null;
    }

    private static String generateAutoCopiedMessage(String prefix,
        String projectName, String versionSlug, String docId, String author) {
        StringBuilder comment = new StringBuilder();

        comment.append(prefix)
            .append(": translation copied from project '")
            .append(projectName)
            .append("', version '")
            .append(versionSlug)
            .append("', document '")
            .append(docId)
            .append("'");

        if (!StringUtils.isEmpty(author)) {
            comment.append(", author '").append(author).append("'");
        }
        return comment.toString();
    }

    /**
     * Returns the entity ID for the original entity (if 'from' is itself
     * a copy) or for 'from' (if not a copy)
     * @param from
     * @return
     */
    public static Long getCopiedEntityId(@Nonnull ITextFlowTargetHistory from) {
        return from.getCopiedEntityId() == null ? from.getId() : from
                .getCopiedEntityId();
    }

    /**
     * Returns the entity type for the original entity (if 'from' is itself
     * a copy) or for 'from' (if not a copy)
     * @param from
     * @return
     */
    @Nullable
    public static EntityType getCopiedEntityType(
        @Nonnull ITextFlowTargetHistory from) {
        if (from.getCopiedEntityType() != null) {
            return from.getCopiedEntityType();
        } else {
            if (from instanceof HTextFlowTarget) {
                return EntityType.HTexFlowTarget;
            } else if (from instanceof HTextFlowTargetHistory) {
                return EntityType.HTextFlowTargetHistory;
            }
            throw new RuntimeException("unexpected type " + from.getClass());
        }
    }
}
