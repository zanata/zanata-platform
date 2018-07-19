package org.zanata.webtrans.client.ui;

import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import org.zanata.webtrans.shared.model.DiffMode;
import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TextContentsDisplay {
    private static final String EMPTY_SEARCH_TERM = "";
    private SafeHtml safeHtml;

    private TextContentsDisplay(Iterable<String> contents,
            String highlightString, Map<String, String> attributes) {
        safeHtml = convertToSafeHtml(contents, highlightString, attributes);
    }

    private TextContentsDisplay(List<String> originalContents,
        List<String> diffContent, DiffMode diffMode,
        Map<String, String> attributes) {
        if (diffMode == DiffMode.HIGHLIGHT) {
            safeHtml =
                convertToSafeHtmlAsDiffHighlight(originalContents,
                    diffContent, attributes);
        } else {
            safeHtml = convertToSafeHtmlAsDiff(originalContents, diffContent,
                attributes);
        }
    }

    public static TextContentsDisplay asSyntaxHighlightAndSearch(
        Iterable<String> contents, String highlightString) {
        return new TextContentsDisplay(contents, highlightString, null);
    }

    public static TextContentsDisplay asSyntaxHighlight(
            Iterable<String> contents, Map<String, String> attributes) {
        return new TextContentsDisplay(contents, EMPTY_SEARCH_TERM, attributes);
    }

    public static TextContentsDisplay asSyntaxHighlight(
        Iterable<String> contents) {
        return asSyntaxHighlight(contents, null);
    }

    public static TextContentsDisplay asDiff(List<String> originalContents,
            List<String> diffContents) {
        return new TextContentsDisplay(originalContents, diffContents,
                DiffMode.NORMAL, null);
    }

    public static TextContentsDisplay asDiffHighlight(
        List<String> originalContents, List<String> diffContents) {
        return new TextContentsDisplay(originalContents, diffContents,
            DiffMode.HIGHLIGHT, null);
    }

    private static void setAttributes(Element element,
        Map<String, String> attributes) {
        if (attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                element.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    private static SafeHtml convertToSafeHtmlAsDiff(
        List<String> originalContents, List<String> diffContents,
        Map<String, String> attributes) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        for (int i = 0; i < originalContents.size(); i++) {
            DiffMatchPatchLabel label = DiffMatchPatchLabel.normalDiff();
            label.setOriginal(originalContents.get(i));
            label.setText(diffContents.get(i));
            setAttributes(label.getElement().getFirstChildElement(), attributes);
            appendContent(builder, label.getElement().getString());
        }
        return builder.toSafeHtml();
    }

    private static SafeHtml convertToSafeHtmlAsDiffHighlight(
        List<String> originalContents, List<String> diffContents,
        Map<String, String> attributes) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        for (int i = 0; i < originalContents.size(); i++) {
            DiffMatchPatchLabel label = DiffMatchPatchLabel.highlightDiff();
            label.setOriginal(originalContents.get(i));
            label.setText(diffContents.get(i));
            setAttributes(label.getElement().getFirstChildElement(), attributes);
            appendContent(builder, label.getElement().getString());
        }
        return builder.toSafeHtml();
    }

    private static SafeHtml convertToSafeHtml(Iterable<String> contents,
        String highlightString, Map<String, String> attributes) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        for (String content : contents) {
            HighlightingLabel label = new HighlightingLabel(content);
            if (!Strings.isNullOrEmpty(highlightString)) {
                label.highlight(highlightString);
            }
            setAttributes(label.getElement().getFirstChildElement(), attributes);
            appendContent(builder, label.getElement().getString());
        }
        return builder.toSafeHtml();
    }

    private static void appendContent(SafeHtmlBuilder sb, String content) {
        sb.appendHtmlConstant("<div class='textFlowEntry'>")
                .appendHtmlConstant(content).appendHtmlConstant("</div>");
    }

    public SafeHtml toSafeHtml() {
        return safeHtml;
    }
}
