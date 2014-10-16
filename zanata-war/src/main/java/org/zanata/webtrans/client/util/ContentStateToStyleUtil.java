package org.zanata.webtrans.client.util;

import org.zanata.common.ContentState;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ContentStateToStyleUtil {
    private ContentStateToStyleUtil() {
    }

    public static String stateToStyle(ContentState state, String initialStyle) {
        String styleNames = initialStyle;
        if (state == ContentState.Approved) {
            styleNames += " ApprovedStateDecoration";
        } else if (state == ContentState.NeedReview) {
            styleNames += " FuzzyStateDecoration";
        } else if (state == ContentState.Rejected) {
            styleNames += " RejectedStateDecoration";
        } else if (state == ContentState.Translated) {
            styleNames += " TranslatedStateDecoration";
        }
        return styleNames;
    }

    public static String stateToStyle(ContentState state) {
        String styleNames = "";
        if (state == null) {
            return styleNames;
        }
        switch (state) {
            case New:
                return "txt--state-neutral";
            case NeedReview:
                return "txt--state-unsure";
            case Translated:
                return "txt--state-success";
            case Approved:
                return "txt--state-highlight";
            case Rejected:
                return "txt--state-danger";
        }
        return styleNames;
    }
}
