/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HApplicationConfiguration extends ModelEntityBase {

    public static String KEY_HOST = "host.url";
    public static String KEY_REGISTER = "register.url";
    public static String KEY_DOMAIN = "email.domain";
    public static String KEY_ADMIN_EMAIL = "email.admin.addr";
    public static String KEY_EMAIL_FROM_ADDRESS = "email.from.addr";
    public static String KEY_HOME_CONTENT = "pages.home.content";
    public static String KEY_HELP_CONTENT = "pages.help.content";
    public static String KEY_LOG_DESTINATION_EMAIL = "log.destination.email";
    public static String KEY_EMAIL_LOG_EVENTS = "log.email.active";
    public static String KEY_EMAIL_LOG_LEVEL = "log.email.level";
    public static String KEY_PIWIK_URL = "piwik.url";
    public static String KEY_PIWIK_IDSITE = "piwik.idSite";
    private static final long serialVersionUID = 8652817113098817448L;

    private String key;
    private String value;

    // TODO PERF @NaturalId(mutable=false) for better criteria caching
    @NaturalId
    @NotEmpty
    @Size(max = 255)
    @Column(name = "config_key", nullable = false)
    public String getKey() {
        return key;
    }

    @NotNull
    @Type(type = "text")
    @Column(name = "config_value", nullable = false)
    public String getValue() {
        return value;
    }
}
