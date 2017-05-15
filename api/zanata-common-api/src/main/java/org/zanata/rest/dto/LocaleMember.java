/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import java.io.Serializable;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LocaleMember implements Serializable {

    private static final long serialVersionUID = 7094715479753034419L;
    private final String username;
    private final Boolean isCoordinator;
    private final Boolean isReviewer;
    private final Boolean isTranslator;


    public LocaleMember(String username, Boolean isCoordinator,
            Boolean isReviewer, Boolean isTranslator) {
        this.username = username;
        this.isCoordinator = isCoordinator;
        this.isReviewer = isReviewer;
        this.isTranslator = isTranslator;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("isCoordinator")
    public Boolean getCoordinator() {
        return isCoordinator;
    }

    @JsonProperty("isReviewer")
    public Boolean getReviewer() {
        return isReviewer;
    }

    @JsonProperty("isTranslator")
    public Boolean getTranslator() {
        return isTranslator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocaleMember that = (LocaleMember) o;

        if (username != null ? !username.equals(that.username) :
                that.username != null) return false;
        if (isCoordinator != null ? !isCoordinator.equals(that.isCoordinator) :
                that.isCoordinator != null) return false;
        if (isReviewer != null ? !isReviewer.equals(that.isReviewer) :
                that.isReviewer != null) return false;
        return isTranslator != null ? isTranslator.equals(that.isTranslator) :
                that.isTranslator == null;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result =
                31 * result +
                        (isCoordinator != null ? isCoordinator.hashCode() : 0);
        result = 31 * result + (isReviewer != null ? isReviewer.hashCode() : 0);
        result = 31 * result +
                (isTranslator != null ? isTranslator.hashCode() : 0);
        return result;
    }
}
