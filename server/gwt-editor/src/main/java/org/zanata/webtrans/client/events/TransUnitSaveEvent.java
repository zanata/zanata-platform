/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.List;

import com.google.common.base.MoreObjects;
import org.zanata.common.ContentState;
import org.zanata.common.util.ContentStateUtil;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitSaveEvent extends GwtEvent<TransUnitSaveEventHandler> {
    public static final Type<TransUnitSaveEventHandler> TYPE =
            new Type<>();

    private TransUnitId transUnitId;
    private Integer verNum;
    private List<String> oldContents = Lists.newArrayList();
    private List<String> targets = Lists.newArrayList();
    private ContentState status;
    private ContentState adjustedState;

    public TransUnitSaveEvent(List<String> targets, ContentState status,
            TransUnitId transUnitId, Integer verNum, List<String> oldContents) {
        this.targets = targets;
        this.status = status;
        this.transUnitId = transUnitId;
        this.verNum = verNum;
        this.oldContents = oldContents;
        adjustedState = adjustState(targets, status);
    }

    public Type<TransUnitSaveEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(TransUnitSaveEventHandler handler) {
        handler.onTransUnitSave(this);
    }

    public List<String> getTargets() {
        return targets;
    }

    public ContentState getStatus() {
        return status;
    }

    public ContentState getAdjustedStatus() {
        return adjustedState;
    }

    public TransUnitId getTransUnitId() {
        return transUnitId;
    }

    public Integer getVerNum() {
        return verNum;
    }

    public List<String> getOldContents() {
        return oldContents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TransUnitSaveEvent that = (TransUnitSaveEvent) o;
        return Objects.equal(transUnitId, that.transUnitId)
                && Objects.equal(verNum, that.verNum);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(transUnitId, verNum, targets, status);
    }

    /**
     *
     *
     * @param newContents
     *            new target contents
     * @param requestedState
     *            requested state by user
     * @see org.zanata.service.impl.TranslationServiceImpl#adjustContentsAndState
     */
    public static ContentState adjustState(List<String> newContents,
            ContentState requestedState) {
        if (newContents == null) {
            return ContentState.New;
        }

        return ContentStateUtil.determineState(requestedState, newContents);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("transUnitId", transUnitId)
                .add("verNum", verNum).toString();
    }
}
