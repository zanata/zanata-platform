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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotEmpty;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import io.leangen.graphql.annotations.types.GraphQLType;
// FIXME Cacheable

@Entity
@GraphQLType(name = "ApplicationConfiguration")
public class HApplicationConfiguration extends ModelEntityBase {

    // Please keep these constants sorted!
    public static final String KEY_ADMIN_EMAIL = "email.admin.addr";
    public static final String KEY_ALLOW_ANONYMOUS_USER =
            "allow.anonymous.user";
    public static final String KEY_AUTO_ACCEPT_TRANSLATOR =
            "accept.translator.requests";
    public static final String KEY_DISPLAY_USER_EMAIL = "display.user.email";
    public static final String KEY_DOMAIN = "email.domain";
    public static final String KEY_EMAIL_FROM_ADDRESS = "email.from.addr";
    public static final String KEY_EMAIL_LOG_EVENTS = "log.email.active";
    public static final String KEY_EMAIL_LOG_LEVEL = "log.email.level";
    public static final String KEY_GRAVATAR_RATING = "gravatar.rating";
    /**
     * obsolete data key. Help content was removed from UI but the content was
     * left in the database for reference purposes.
     */
    @Deprecated
    public static final String KEY_HELP_CONTENT = "pages.help.content";
    public static final String KEY_HELP_URL = "help.url";
    public static final String KEY_HOME_CONTENT = "pages.home.content";
    public static final String KEY_HOST = "host.url";
    public static final String KEY_LOG_DESTINATION_EMAIL =
            "log.destination.email";
    public static final String KEY_MAX_ACTIVE_REQ_PER_API_KEY =
            "max.active.req.per.apikey";
    public static final String KEY_MAX_CONCURRENT_REQ_PER_API_KEY =
            "max.concurrent.req.per.apikey";
    public static final String KEY_MAX_FILES_PER_UPLOAD =
            "fileupload.max.files.per.upload";
    public static final String KEY_PERMITTED_USER_EMAIL_DOMAIN =
            "permitted.user.email.domain";
    public static final String KEY_PIWIK_IDSITE = "piwik.idSite";
    public static final String KEY_PIWIK_URL = "piwik.url";
    public static final String KEY_REGISTER = "register.url";
    public static final String KEY_TERMS_CONDITIONS_URL =
            "terms.conditions.url";
    public static final String KEY_TM_FUZZY_BANDS = "tm.fuzzy.bands";

    private static final long serialVersionUID = 8652817113098817448L;

    private static List<String> availableKeys;
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
    @javax.persistence.Lob
    @Column(name = "config_value", nullable = false)
    public String getValue() {
        return value;
    }

    /**
     * Using reflection to get defined configuration key constants in
     * HApplicationConfiguration.
     */
    public static List<String> getAvailableKeys() {
        if (availableKeys != null) {
            return availableKeys;
        }
        final HApplicationConfiguration dummy = new HApplicationConfiguration();
        Field[] availableConfigKeys =
                (HApplicationConfiguration.class.getFields());
        availableKeys = Arrays.stream(availableConfigKeys).map(field -> {
            try {
                field.setAccessible(true);
                return (String) field.get(dummy);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        return availableKeys;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public HApplicationConfiguration() {
    }

    @java.beans.ConstructorProperties({ "key", "value" })
    public HApplicationConfiguration(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Business equality comparison
     * @param other
     * @return other is equal
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        if (!super.equals(other)) return false;

        HApplicationConfiguration that = (HApplicationConfiguration) other;

        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
