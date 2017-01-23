/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.model;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SlugEntityBaseTest {

    private static final String DELETED_SUFFIX = "_.-1234567890";
    // all ModelEntityBase descendants must override equals and hashcode to
    // check
    // their types.

    static class SlugClass extends SlugEntityBase {

        public SlugClass(String slug) {
            super(slug);
        }

        @Override
        protected String deletedSlugSuffix() {
            return DELETED_SUFFIX;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof SlugEntityBaseTest.SlugClass))
                return false;
            final SlugClass other = (SlugClass) o;
            if (!other.canEqual((Object) this))
                return false;
            if (!super.equals(o))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof SlugEntityBaseTest.SlugClass;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + super.hashCode();
            return result;
        }

        public SlugClass() {
        }
    }

    @Test
    public void lombokToStringAndEqualsTest() {
        SlugEntityBase entity = new SlugClass();
        entity.setSlug("abc");
        entity.setId(1L);
        entity.setVersionNum(2);
        assertThat(entity.toString(),
                containsString("[id=1,versionNum=2], slug=abc)"));
        SlugEntityBase other = new SlugClass("abc");
        assertThat(entity.equals(other), equalTo(false));
        other.setId(entity.getId());
        other.setVersionNum(entity.getVersionNum());
        assertThat(entity, equalTo(other));
        assertThat(entity.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void changeToDeletedSlug() {
        SlugEntityBase slugEntityBase = new SlugClass("abc");
        String newSlug = slugEntityBase.changeToDeletedSlug();
        Assertions.assertThat(newSlug).isEqualTo("abc" + DELETED_SUFFIX);
    }

    @Test
    public void canChangeToDeletedSlugWithSuffixInPlaceIfOldSlugIsTooLong() {
        // 36 characters long
        SlugEntityBase slugEntityBase =
                new SlugClass("abcdefghijklmnopqrstuvwxyz1234567890");
        String newSlug = slugEntityBase.changeToDeletedSlug();
        Assertions.assertThat(newSlug)
                .isEqualTo("abcdefghijklmnopqrstuvwxyz1" + DELETED_SUFFIX)
                .hasSize(40);
    }
}
