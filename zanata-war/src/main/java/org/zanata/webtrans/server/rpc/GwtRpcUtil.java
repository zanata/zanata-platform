/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

import com.google.common.collect.Lists;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
class GwtRpcUtil {
    private static final Logger log = LoggerFactory.getLogger(GwtRpcUtil.class);

    static ArrayList<String> getSourceContents(HTextFlow textFlow) {
        ArrayList<String> sourceContents =
                Lists.newArrayList(textFlow.getContents());
        assert textFlow.isPlural() || sourceContents.size() < 2 : "non-plural textflow with multiple strings: "
                + textFlow.getResId();
        return sourceContents;
    }

    /**
     * Gets the string contents of target (unless null), padding the ArrayList
     * to nPlurals (if the textFlow allows plurals), or to 1 otherwise.
     *
     * @param textFlow
     * @param target
     * @param nPlurals
     * @return
     */
    static ArrayList<String> getTargetContentsWithPadding(HTextFlow textFlow,
            HTextFlowTarget target, int nPlurals) {
        int expectedResultSize = textFlow.isPlural() ? nPlurals : 1;
        ArrayList<String> result = new ArrayList<String>(expectedResultSize);
        if (target != null) {
            List<String> targetContents = target.getContents();
            if (targetContents.size() > expectedResultSize) {
                log.warn(
                        "TextFlowTarget {} has {} strings: trimming excess plurals",
                        target.getId(), targetContents.size());
                result.addAll(targetContents.subList(0, expectedResultSize));
            } else {
                result.addAll(targetContents);
            }
        }
        while (result.size() < expectedResultSize) {
            result.add("");
        }
        return result;
    }
}
