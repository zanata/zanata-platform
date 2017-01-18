/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

package org.zanata.webtrans.shared.model;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TransMemoryQuery implements IsSerializable {
    private SearchType searchType;
    private List<String> queries;
    private Condition project;
    private Condition document;
    private Condition res;
    private Condition includeOwnTranslation = new Condition(true, null);

    @SuppressWarnings("unused")
    private TransMemoryQuery() {
    }

    public TransMemoryQuery(String query, SearchType searchType) {
        this.searchType = searchType;
        this.queries = new ArrayList<String>(1);
        this.queries.add(query);
        if (searchType == SearchType.FUZZY_PLURAL) {
            throw new RuntimeException(
                    "Can't use FUZZY_PLURAL SearchType with a single query string");
        }
    }

    public TransMemoryQuery(List<String> queries, SearchType searchType) {
        this.searchType = searchType;
        this.queries = queries;
        if (searchType != SearchType.FUZZY_PLURAL) {
            throw new RuntimeException(
                    "SearchType must be FUZZY_PLURAL when using multiple query strings");
        }
    }

    public TransMemoryQuery(List<String> queries, SearchType searchType,
            Condition project, Condition document, Condition res) {
        this(queries, searchType);
        this.project = project;
        this.document = document;
        this.res = res;
    }

    public TransMemoryQuery(String query, SearchType searchType,
            Condition project, Condition document, Condition res) {
        this(query, searchType);
        this.project = project;
        this.document = document;
        this.res = res;
    }

    public void setIncludeOwnTranslation(boolean isInclude, String tfId) {
        this.includeOwnTranslation = new Condition(isInclude, tfId);
    }

    public Condition getProject() {
        return project;
    }

    public Condition getDocument() {
        return document;
    }

    public Condition getRes() {
        return res;
    }

    public Condition getIncludeOwnTranslation() {
        return includeOwnTranslation;
    }

    public List<String> getQueries() {
        return queries;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    @Override
    public String toString() {
        return "TransMemoryQuery{" + "searchType=" + searchType + ", queries="
                + queries + ", project=" + project + ", document=" + document
                + ", res=" + res + ", includeOwnTranslation="
                + includeOwnTranslation + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TransMemoryQuery that = (TransMemoryQuery) o;

        if (document != null ? !document.equals(that.document)
                : that.document != null)
            return false;
        if (project != null ? !project.equals(that.project)
                : that.project != null)
            return false;
        if (queries != null ? !queries.equals(that.queries)
                : that.queries != null)
            return false;
        if (res != null ? !res.equals(that.res) : that.res != null)
            return false;
        if (searchType != that.searchType)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = searchType != null ? searchType.hashCode() : 0;
        result = 31 * result + (queries != null ? queries.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (document != null ? document.hashCode() : 0);
        result = 31 * result + (res != null ? res.hashCode() : 0);
        return result;
    }

    public static class Condition implements IsSerializable {
        private boolean isCheck;
        private String value;

        public Condition(boolean isCheck, String value) {
            this.isCheck = isCheck;
            this.value = value;
        }

        public boolean isCheck() {
            return isCheck;
        }

        public String getValue() {
            return value;
        }

        @SuppressWarnings("unused")
        private Condition() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Condition condition = (Condition) o;

            if (isCheck != condition.isCheck)
                return false;
            if (value != null ? !value.equals(condition.value)
                    : condition.value != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (isCheck ? 1 : 0);
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Condition{" + "isCheck=" + isCheck + ", value='" + value
                    + '\'' + '}';
        }
    }

}
