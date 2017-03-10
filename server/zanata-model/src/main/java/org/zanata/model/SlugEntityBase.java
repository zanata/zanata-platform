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

import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.NaturalId;
import org.hibernate.search.annotations.Field;
import org.zanata.model.validator.Slug;
import com.google.common.annotations.VisibleForTesting;

@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class SlugEntityBase extends ModelEntityBase {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SlugEntityBase.class);

    /**
     * We append this suffix to a deleted slug entity so that its original slug
     * become available to use.
     */
    private static final String DELETED_SLUG_SUFFIX = "_.-";
    private static final long serialVersionUID = -1911540675412928681L;
    @NaturalId(mutable = true)
    @Size(min = 1, max = 40)
    @Slug
    @NotNull
    @Field
    private String slug;

    /**
     * If the slug entity is set to obsolete (soft delete), we need to recycle
     * its slug. This will suffix the original slug with deleted slug suffix
     * plus a timestamp. if original slug is too long, the suffix will be put
     * into the end and replacing some of the old slug characters. This means we
     * won't be able to recover what the old slug was.
     *
     * @return an artificial slug just so the old slug will become available to
     *         use.
     */
    @Transient
    public String changeToDeletedSlug() {
        String deletedSlugSuffix = deletedSlugSuffix();
        String newSlug = slug + deletedSlugSuffix;
        if (newSlug.length() <= 40) {
            return newSlug;
        } else {
            newSlug = slug.substring(0, 40 - deletedSlugSuffix.length())
                    + deletedSlugSuffix;
            log.warn(
                    "Entity [{}] old slug [{}] is too long to apply suffix. We will add suffix in place [{}]",
                    this, slug, newSlug);
            return newSlug;
        }
    }

    @VisibleForTesting
    protected String deletedSlugSuffix() {
        int timeSuffix = new Date().hashCode();
        return DELETED_SLUG_SUFFIX + timeSuffix;
    }

    @Override
    public String toString() {
        return "SlugEntityBase(super=" + super.toString() + ", slug="
                + this.getSlug() + ")";
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return this.slug;
    }

    @java.beans.ConstructorProperties({ "slug" })
    public SlugEntityBase(final String slug) {
        this.slug = slug;
    }

    public SlugEntityBase() {
    }
}
