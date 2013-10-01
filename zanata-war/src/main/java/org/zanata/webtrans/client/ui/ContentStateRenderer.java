/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.ui;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.resources.EnumMessages;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class ContentStateRenderer extends EnumRenderer<ContentState> {
    private final EnumMessages messages;

    @Inject
    public ContentStateRenderer(EnumMessages messages) {
        this.messages = messages;
    }

    @Override
    public String render(ContentState object) {
        if (object == null) {
            return messages.contentStateUnsaved();
        }
        switch (object) {
        case New:
            return messages.contentStateUntranslated();
        case NeedReview:
            return messages.contentStateFuzzy();
        case Translated:
            return messages.contentStateTranslated();
        case Approved:
            return messages.contentStateApproved();
        case Rejected:
            return messages.contentStateRejected();
        default:
            return getEmptyValue();
        }
    }
}
