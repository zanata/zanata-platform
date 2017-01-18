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

package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.event.shared.GwtEvent;

public class ReviewCommentEvent extends GwtEvent<ReviewCommentEventHandler> {
    public static final Type<ReviewCommentEventHandler> TYPE =
            new Type<>();

    private TransUnitId transUnitId;

    public ReviewCommentEvent(TransUnitId transUnitId) {
        this.transUnitId = transUnitId;
    }

    public TransUnitId getTransUnitId() {
        return transUnitId;
    }

    public Type<ReviewCommentEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ReviewCommentEventHandler handler) {
        handler.onShowReviewComment(this);
    }
}
