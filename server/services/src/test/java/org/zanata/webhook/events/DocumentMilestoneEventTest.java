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
package org.zanata.webhook.events;

import org.junit.Test;
import org.zanata.common.LocaleId;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author djansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class DocumentMilestoneEventTest {

    @Test
    public void testEventFields() {
        DocumentMilestoneEvent event = new DocumentMilestoneEvent(
            "myproject", "master",
             "about_fedora", new LocaleId("en"), "Document",
            "http://localhost:8080/server/url");

        assertThat(event.getProject()).isEqualTo("myproject");
        assertThat(event.getVersion()).isEqualTo("master");
        assertThat(event.getDocId()).isEqualTo("about_fedora");
        assertThat(event.getLocale()).isEqualTo(new LocaleId("en"));
        assertThat(event.getMilestone()).isEqualTo("Document");
        assertThat(event.getEditorDocumentUrl())
                .isEqualTo("http://localhost:8080/server/url");

        event = new DocumentMilestoneEvent();
        event.setProject("testproject");
        event.setVersion("testversion");
        event.setDocId("about_rhel");
        event.setLocale(new LocaleId("fr"));
        event.setMilestone("Translation milestone");
        event.setEditorDocumentUrl("http://localhost:8080/test/url");

        assertThat(event.getProject()).isEqualTo("testproject");
        assertThat(event.getVersion()).isEqualTo("testversion");
        assertThat(event.getDocId()).isEqualTo("about_rhel");
        assertThat(event.getLocale()).isEqualTo(new LocaleId("fr"));
        assertThat(event.getMilestone()).isEqualTo("Translation milestone");
        assertThat(event.getEditorDocumentUrl())
                .isEqualTo("http://localhost:8080/test/url");

        assertThat(event.getType()).isEqualTo("DocumentMilestoneEvent");
    }

    @Test
    public void testEquals() {
        DocumentMilestoneEvent event = new DocumentMilestoneEvent(
                "myproject", "master",
                "about_fedora", new LocaleId("en"), "Document",
                "http://localhost:8080/server/url"
        );

        assertThat(event.equals(new DocumentMilestoneEvent(
                "myproject", "master",
                "about_fedora", new LocaleId("en"), "Document",
                "http://localhost:8080/server/url"
        ))).isTrue();
        assertThat(event.equals(event)).isTrue();
        assertThat(event.equals(new LocaleId("en"))).isFalse();
        assertThat(event.equals(new DocumentMilestoneEvent(
                "testproject", "master",
                "about_fedora", new LocaleId("en"), "Document",
                "http://localhost:8080/server/url"
        ))).isFalse();
        assertThat(event.equals(new DocumentMilestoneEvent(
                "myproject", "testversion",
                "about_fedora", new LocaleId("en"), "Document",
                "http://localhost:8080/server/url"
        ))).isFalse();
        assertThat(event.equals(new DocumentMilestoneEvent(
                "myproject", "master",
                "about_rhel", new LocaleId("en"), "Document",
                "http://localhost:8080/server/url"
        ))).isFalse();
        assertThat(event.equals(new DocumentMilestoneEvent(
                "myproject", "master",
                "about_fedora", new LocaleId("fr"), "Document",
                "http://localhost:8080/server/url"
        ))).isFalse();
        assertThat(event.equals(new DocumentMilestoneEvent(
                "myproject", "master",
                "about_fedora", new LocaleId("en"), "Translation milestone",
                "http://localhost:8080/server/url"
        ))).isFalse();
        assertThat(event.equals(new DocumentMilestoneEvent(
                "myproject", "master",
                "about_fedora", new LocaleId("en"), "Document",
                "http://localhost:8080/test/url"
        ))).isFalse();
    }

    @Test
    public void testHashCode() {
        DocumentMilestoneEvent event = new DocumentMilestoneEvent(
                "myproject", "master",
                "about_fedora", new LocaleId("en"), "Document",
                "http://localhost:8080/server/url");
        assertThat(event.hashCode()).isEqualTo(
                new DocumentMilestoneEvent(
                    "myproject", "master",
                    "about_fedora", new LocaleId("en"), "Document",
                    "http://localhost:8080/server/url").hashCode());
        assertThat(event.hashCode()).isNotEqualTo(
                new DocumentMilestoneEvent(
                        "myproject", "version",
                        "about_fedora", new LocaleId("en"), "Document",
                        "http://localhost:8080/server/url").hashCode());
    }
}
