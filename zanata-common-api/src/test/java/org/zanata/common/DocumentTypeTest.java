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
package org.zanata.common;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.zanata.common.DocumentType.*;

import java.util.List;
import java.util.Set;

public class DocumentTypeTest {

    @Test
    public void typeForExtantType() {
        assertThat(fromSourceExtension("txt"), contains(PLAIN_TEXT));
        assertThat(fromSourceExtension("srt"), contains(SUBTITLE));
    }

    @Test
    public void typeForNonExistentTypeCurrentBehaviour() {
        assertThat(fromSourceExtension("unknown"), hasSize(0));
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void typeForNonExistentTypeBetterBehaviour() {
        fromSourceExtension("unknown");
    }

    @Test
    public void typeForKnownTypeAfterDot() {
        assertThat(fromSourceExtension(".txt"), hasSize(0));
    }

    @Test
    public void typeForKnownTypeWithPrefix() {
        assertThat(fromSourceExtension("foo.txt"), hasSize(0));
    }

    @Test
    public void getAllSourceExtensionsNotEmpty() {
        Set<String> allExtensions = getAllSourceExtensions();
        assertThat(allExtensions, not(empty()));
        assertThat(
                allExtensions,
                containsInAnyOrder("pot", "txt", "dtd", "idml", "html",
                        "htm", "odt", "fodt", "odp", "fodp", "ods", "fods",
                        "odg", "fodg", "odb", "odf", "srt", "sbt", "sub",
                        "vtt", "properties", "xlf", "xml", "ts"));
    }

    @Test
    public void getAllTranslationExtensionsNotEmpty() {
        Set<String> allExtensions = getAllTranslationExtensions();
        assertThat(allExtensions, not(empty()));
        assertThat(
            allExtensions,
            containsInAnyOrder("po", "txt", "dtd", "idml", "html",
                "htm", "odt", "fodt", "odp", "fodp", "ods", "fods",
                "odg", "fodg", "odb", "odf", "srt", "sbt", "sub",
                "vtt", "properties", "xlf", "xml", "ts"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getAllExtensionsReadOnlyCannotAdd() {
        Set<String> allExtensions = getAllSourceExtensions();
        allExtensions.add("newExtension");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getAllExtensionsReadOnlyCannotClear() {
        Set<String> allExtensions = getAllSourceExtensions();
        allExtensions.clear();
    }

    @Test
    public void getExtensionsHasCorrectValues() {
        // given: HTML has extensions "html" and "htm"
        assertThat(HTML.getSourceExtensions().contains("html"), is(true));
        assertThat(HTML.getSourceExtensions().contains("htm"), is(true));
        assertThat(HTML.getSourceExtensions().contains("idml"), is(false));
    }
}
