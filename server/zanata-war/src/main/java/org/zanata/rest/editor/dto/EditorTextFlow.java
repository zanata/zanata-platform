package org.zanata.rest.editor.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.TextFlow;

import java.util.Date;

/**
 * TextFlow with added fields needed by the editor.
 *
 * This class holds extra TextFlow metadata for serialization so that it is
 * available in the editor without extra network traffic.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "id", "lang", "content", "contents", "plural",
    "extensions", "wordCount", "msgctxt", "sourceReferences", "sourceFlags",
    "sourceComment" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EditorTextFlow extends TextFlow {

    private int wordCount;
    private String msgctxt;
    private String sourceReferences;
    private String sourceFlags;
    private String sourceComment;

    public EditorTextFlow() {
        super(null, null, (String) null);
    }

    public EditorTextFlow(String id, LocaleId lang) {
        super(id, lang);
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getMsgctxt() {
        return msgctxt;
    }

    public void setMsgctxt(String msgctxt) {
        this.msgctxt = msgctxt;
    }

    public String getSourceReferences() {
        return sourceReferences;
    }

    public void setSourceReferences(String sourceReferences) {
        this.sourceReferences = sourceReferences;
    }

    public String getsourceFlags() {
        return sourceFlags;
    }

    public void setsourceFlags(String sourceFlags) {
        this.sourceFlags = sourceFlags;
    }

    public String getSourceComment() {
        return sourceComment;
    }

    public void setSourceComment(String sourceComment) {
        this.sourceComment = sourceComment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EditorTextFlow that = (EditorTextFlow) o;

        if (wordCount != that.wordCount) return false;
        if (msgctxt != null ? !msgctxt.equals(that.msgctxt) :
                that.msgctxt != null)
            return false;
        if (sourceReferences != null ?
                !sourceReferences.equals(that.sourceReferences) :
                that.sourceReferences != null) return false;
        if (sourceFlags != null ? !sourceFlags.equals(that.sourceFlags) :
                that.sourceFlags != null) return false;
        return sourceComment != null ?
                sourceComment.equals(that.sourceComment) :
                that.sourceComment == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + wordCount;
        result = 31 * result + (msgctxt != null ? msgctxt.hashCode() : 0);
        result = 31 * result +
                (sourceReferences != null ? sourceReferences.hashCode() : 0);
        result = 31 * result +
                (sourceFlags != null ? sourceFlags.hashCode() : 0);
        result =
                31 * result +
                        (sourceComment != null ? sourceComment.hashCode() : 0);
        return result;
    }
}
