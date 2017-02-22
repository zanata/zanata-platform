package org.zanata.rest.editor.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.TextFlow;

/**
 * TextFlow with added fields needed by the editor.
 *
 * This class holds extra TextFlow metadata for serialization so that it is
 * available in the editor without extra network traffic.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "id", "lang", "content", "contents", "plural",
    "extensions", "wordCount" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EditorTextFlow extends TextFlow {

    public EditorTextFlow() {
        super(null, null, (String) null);
    }

    public EditorTextFlow(String id, LocaleId lang) {
        super(id, lang);
    }
    private int wordCount;

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }
}
