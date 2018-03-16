/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.shared.rest.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.zanata.webtrans.shared.model.ProjectIterationId;

import com.google.common.base.MoreObjects;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Criteria for TM merge from internal TM.
 */
public class InternalTMSource implements Serializable, IsSerializable {

    public static final InternalTMSource SELECT_ALL = new InternalTMSource(InternalTMChoice.SelectAny, null);
    public static final InternalTMSource SELECT_NONE = new InternalTMSource(InternalTMChoice.SelectNone, null);
    private static final long serialVersionUID = 1L;
    private InternalTMChoice choice;
    private List<ProjectIterationId> projectIterationIds;
    @JsonIgnore
    private List<Long> filteredProjectVersionIds = Collections.emptyList();

    public InternalTMSource(InternalTMChoice choice,
            List<ProjectIterationId> projectIterationIds) {
        this.choice = choice;
        this.projectIterationIds = projectIterationIds;
    }

    public InternalTMSource() {
    }

    public List<ProjectIterationId> getProjectIterationIds() {
        return projectIterationIds;
    }

    public InternalTMChoice getChoice() {
        return choice;
    }

    @Override
    public String toString() {
        // SELECT_ALL
        if (choice.equals(InternalTMChoice.SelectAny) && projectIterationIds == null) {
            return "Select internal TM from any project version";
        }
        return MoreObjects.toStringHelper(this)
                .add("choice", choice)
                .add("projectIterationIds", projectIterationIds)
                .toString();
    }

    public void setFilteredProjectVersionIds(
            List<Long> filteredProjectVersionIds) {
        this.filteredProjectVersionIds = filteredProjectVersionIds;
    }

    public List<Long> getFilteredProjectVersionIds() {
        return filteredProjectVersionIds;
    }

    public enum InternalTMChoice {
        SelectNone, SelectAny, SelectSome

    }
}
