/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.adapter.asciidoc;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.resource.RawDocument;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 **/
public class AsciidocFilterTest {
    String resourcePath = "src/test/resources/org/zanata/adapter/test.adoc";
    String basicResource = "src/test/resources/org/zanata/adapter/basic.adoc";
    @Test
    public void test() throws IOException {
        File adoc = new File(resourcePath);
        String fileContent = FileUtils.readFileToString(adoc);
        try (AsciidocFilter filter = new AsciidocFilter()) {
            RawDocument doc = new RawDocument(fileContent, null);
            ArrayList<Event> events = new ArrayList<>();
            filter.open(doc);
            while (filter.hasNext()) {
                events.add(filter.next());
            }

            assertEquals(80, events.size());
            assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());
            assertEquals(EventType.END_DOCUMENT, events.get(79).getEventType());
        }
    }
}
