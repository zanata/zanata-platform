/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.webtrans.shared.rpc;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EditorFilter implements IsSerializable {
    public static final EditorFilter ALL = new EditorFilter();
    private String textInContent;
    private String resId;
    private String lastModifiedBefore;
    private String lastModifiedAfter;
    private String lastModifiedByUser;
    private String sourceComment;
    private String transComment;
    private String msgContext;

    private EditorFilter() {
    }

    public static EditorFilter fromQuery(String query) {
        return QueryParser.parse(query);
    }

    public EditorFilter(String textInContent, String resId,
            String lastModifiedBefore, String lastModifiedAfter,
            String lastModifiedByUser, String sourceComment,
            String transComment, String msgContext) {
        this.textInContent = textInContent;
        this.resId = resId;
        this.lastModifiedBefore = lastModifiedBefore;
        this.lastModifiedAfter = lastModifiedAfter;
        this.lastModifiedByUser = lastModifiedByUser;
        this.sourceComment = sourceComment;
        this.transComment = transComment;
        this.msgContext = msgContext;
    }

    public EditorFilter(EditorFilter o) {
        this.textInContent = o.textInContent;
        this.resId = o.resId;
        this.lastModifiedBefore = o.lastModifiedBefore;
        this.lastModifiedAfter = o.lastModifiedAfter;
        this.lastModifiedByUser = o.lastModifiedByUser;
        this.sourceComment = o.sourceComment;
        this.transComment = o.transComment;
        this.msgContext = o.msgContext;
    }

    public String getTextInContent() {
        return textInContent;
    }

    public String getResId() {
        return trimIfNotNull(resId);
    }

    public String getLastModifiedBefore() {
        return trimIfNotNull(lastModifiedBefore);
    }

    public String getLastModifiedAfter() {
        return trimIfNotNull(lastModifiedAfter);
    }

    public String getLastModifiedByUser() {
        return trimIfNotNull(lastModifiedByUser);
    }

    public String getSourceComment() {
        return sourceComment;
    }

    public String getTransComment() {
        return transComment;
    }

    public String getMsgContext() {
        return msgContext;
    }

    private static String trimIfNotNull(String field) {
        return field == null ? null : field.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EditorFilter that = (EditorFilter) o;

        return Objects.equal(lastModifiedAfter, that.lastModifiedAfter)
                && Objects.equal(lastModifiedBefore, that.lastModifiedBefore)
                && Objects.equal(lastModifiedByUser, that.lastModifiedByUser)
                && Objects.equal(msgContext, that.msgContext)
                && Objects.equal(resId, that.resId)
                && Objects.equal(sourceComment, that.sourceComment)
                && Objects.equal(textInContent, that.textInContent)
                && Objects.equal(transComment, that.transComment);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(textInContent, resId, lastModifiedAfter,
                lastModifiedBefore, lastModifiedByUser, sourceComment,
                transComment, msgContext);
    }

    public boolean isAcceptAll() {
        return this.equals(ALL);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("textInContent", textInContent)
                .add("resId", resId)
                .add("lastModifiedBefore", lastModifiedBefore)
                .add("lastModifiedAfter", lastModifiedAfter)
                .add("lastModifiedByUser", lastModifiedByUser)
                .add("sourceComment", sourceComment)
                .add("transComment", transComment)
                .add("msgContext", msgContext)
                .toString();
    }
}
