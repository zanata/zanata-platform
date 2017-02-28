/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.util.TextFormatUtil;
import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TMMergeProgressEvent extends GwtEvent<TMMergeProgressHandler> {
    public static final Type<TMMergeProgressHandler> TYPE = new Type<>();
    private final long processedTextFlows;
    private final long totalTextFlows;
    private final double percent;


    public TMMergeProgressEvent(long processedTextFlows, long totalTextFlows) {
        this.processedTextFlows = processedTextFlows;
        this.totalTextFlows = totalTextFlows;
        this.percent = processedTextFlows * 100.0 / totalTextFlows;
    }


    public boolean isFinished() {
        return processedTextFlows == totalTextFlows;
    }

    public boolean hasNoTextFlowsToMerge() {
        return totalTextFlows == 0;
    }

    public String getPercentDisplay() {
        return TextFormatUtil.formatPercentage(percent);
    }

    @Override
    public Type<TMMergeProgressHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(TMMergeProgressHandler handler) {
        handler.onTMMergeProgress(this);
    }

    public long getTotalTextFlows() {
        return totalTextFlows;
    }
}
