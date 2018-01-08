/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.action;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.model.validator.EmailDomain;
import org.zanata.model.validator.ZanataEmail;

public interface HasUserDetail {
    String USERNAME_REGEX = "^([a-z\\d][a-z\\d_]*){3,20}$";
    int USERNAME_MAX_LENGTH = 20;

    @ZanataEmail
    @NotEmpty
    @EmailDomain
    String getEmail();

    @NotEmpty
    @Size(min = 3, max = USERNAME_MAX_LENGTH)
    @Pattern(regexp = USERNAME_REGEX,
            message = "{validation.username.constraints}")
    String getUsername();
}
