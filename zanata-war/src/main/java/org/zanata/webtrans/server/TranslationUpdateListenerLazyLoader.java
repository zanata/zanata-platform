/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.server;

import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.zanata.util.ServiceLocator;

/**
 * Becasue we can't use CDI in hibernate event listener yet
 * http://stackoverflow.com/a/15078509. We have to do this workaround.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TranslationUpdateListenerLazyLoader implements
        PostUpdateEventListener, PostInsertEventListener {
    private TranslationUpdateListener delegate;

    public void init() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    delegate = ServiceLocator.instance()
                            .getInstance(TranslationUpdateListener.class);
                }
            }
        }
    }

    @Override
    public void onPostInsert(PostInsertEvent postInsertEvent) {
        init();
        delegate.onPostInsert(postInsertEvent);
    }

    @Override
    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        init();
        delegate.onPostUpdate(postUpdateEvent);
    }
}
