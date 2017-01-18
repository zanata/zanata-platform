/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webhook.events;

import org.zanata.common.LocaleId;
import org.zanata.events.WebhookEventType;
import org.zanata.model.type.WebhookType;

import javax.annotation.Nullable;

/**
 * Indicates an webhook event is triggered manually by a user. The fields will
 * contain information about the triggering context. e.g. which project, version
 * and language the user wants to trigger the event for, as well as the
 * triggering user's username.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ManuallyTriggeredEvent extends WebhookEventType {
    private static final String EVENT_TYPE =
            WebhookType.ManuallyTriggeredEvent.name();

    private String zanataServer;
    private String username;
    private String project;
    private String version;
    private LocaleId locale;

    public
    @Nullable
    String getProject() {
        return project;
    }

    public
    @Nullable
    String getVersion() {
        return version;
    }

    public
    @Nullable
    LocaleId getLocale() {
        return locale;
    }

    public String getUsername() {
        return username;
    }

    public String getZanataServer() {
        return zanataServer;
    }

    public ManuallyTriggeredEvent(String zanataServer, String triggeredBy,
            String project,
            String version,
            LocaleId locale) {
        this.zanataServer = zanataServer;
        this.username = triggeredBy;
        this.project = project;
        this.version = version;
        this.locale = locale;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }
}
