package org.zanata.webtrans.shared.model;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author Hannes Eskebaek
 */
public class TextFlowTarget implements HasIdentifier<TextFlowTargetId>, IsSerializable {

    private TextFlowTargetId id;

    private Locale locale;

    private String content;

    // for GWT
    @SuppressWarnings("unused")
    private TextFlowTarget() {
    }

    public TextFlowTarget(TextFlowTargetId id, Locale locale, String content) {
        Preconditions.checkNotNull(id, "localeId cannot be null");
        this.id = id;
        this.locale = locale;
        this.content = content;
    }

    public TextFlowTargetId getId() {
        return id;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getContent() {
        return content;
    }
}
