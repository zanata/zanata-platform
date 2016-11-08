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

package org.zanata.common.util;

import static org.zanata.util.StringUtil.allEmpty;
import static org.zanata.util.StringUtil.allNonEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.zanata.common.ContentState;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class ContentStateUtil {

    /**
     * Canonicalises requested state against contents - eg if state is Approved
     * but some contents are empty, state will be changed to New. Pre-condition:
     * size of contents must match nPlurals (unless message is singular, in
     * which case it should equal 1).
     *
     * @param requestedState
     *            desired ContentState
     * @param contents
     *            message contents which will be checked for emptiness to
     *            determine the ContentState. Must have at least one element.
     * @param resId
     *            used to identify the TextFlowTarget in warning messages
     * @param warnings
     *            a warning string will be added if state is adjusted
     * @return actual legal ContentState
     */
    public static ContentState
            determineState(ContentState requestedState, List<String> contents,
                    String resId, @Nonnull List<String> warnings) {
        // NB make sure this stays consistent with PoReader2.getContentState
        // TODO rhbz953734 - PoReader2.getContentState
        switch (requestedState) {
        case NeedReview:
            if (allEmpty(contents)) {
                warnings.add("Invalid ContentState; changed from NeedReview to New: TextFlowTarget "
                        + resId + " with no contents");
                return ContentState.New;
            }
            break;
        case New:
            if (allNonEmpty(contents)) {
                warnings.add("Invalid ContentState (non-empty contents); changed from New to NeedReview: TextFlowTarget "
                        + resId + " with contents: " + contents);
                return ContentState.NeedReview;
            }
            break;
        case Approved:
            if (!allNonEmpty(contents)) {
                // TODO rhbz953734 this should throw exception if it's require
                // review project
                warnings.add("Invalid ContentState (some empty contents); changed from Approved to New: TextFlowTarget "
                        + resId + " with contents: " + contents);
                return ContentState.New;
            }
            break;
        case Translated:
            if (!allNonEmpty(contents)) {
                warnings.add("Invalid ContentState (some empty contents); changed from Translated to New: TextFlowTarget "
                        + resId + " with contents: " + contents);
                return ContentState.New;
            }
            break;
        case Rejected:
            if (!allNonEmpty(contents)) {
                warnings.add("Invalid ContentState (some empty contents); changed from Rejected to New: TextFlowTarget "
                        + resId + " with contents: " + contents);
                return ContentState.New;
            }
            break;
        default:
            throw new RuntimeException("unknown ContentState " + requestedState);
        }
        return requestedState;
    }

    /**
     * Canonicalises requested state against contents. Convenience method for
     * when warnings are not needed.
     *
     * @param requestedState
     * @param contents
     * @return
     * @see #determineState(ContentState, List, String, List)
     */
    public static ContentState determineState(ContentState requestedState,
            List<String> contents) {
        return determineState(requestedState, contents, "",
                new ArrayList<String>(1));
    }

}
