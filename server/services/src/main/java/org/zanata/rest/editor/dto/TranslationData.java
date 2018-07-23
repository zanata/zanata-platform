package org.zanata.rest.editor.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.ContentState;

import com.google.common.collect.Lists;

/**
 * Class for information on translation updates requested from client and
 * response from server.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "id", "revision", "status", "content", "contents", "plural" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TranslationData implements Serializable {

    private static final long serialVersionUID = -8798154305879389711L;
    @NotNull
    private Integer id;

    @NotNull
    private Integer revision;

    @JsonProperty("content")
    private String content;

    @JsonProperty("contents")
    private List<String> contents;

    @NotNull
    private ContentState status = ContentState.New;

    @JsonProperty("revisionComment")
    private String revisionComment;

    @JsonProperty("lastModifiedBy")
    private String lastModifiedBy;

    @JsonProperty("lastModifiedDate")
    private Date lastModifiedDate;

    public TranslationData() {

    }

    private boolean plural;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public ContentState getStatus() {
        return status;
    }

    public void setStatus(ContentState status) {
        this.status = status;
    }

    public boolean isPlural() {
        return plural;
    }

    public void setPlural(boolean plural) {
        this.plural = plural;
    }

    public String getRevisionComment() {
        return revisionComment;
    }

    public void setRevisionComment(String comment) {
        this.revisionComment = comment;
    }


    @JsonIgnore
    public List<String> getContents() {
        if(contents == null && content == null) {
            return Collections.<String>emptyList();
        }
        return isPlural() ? contents : Lists.newArrayList(content);
    }

    @JsonIgnore
    public void setContents(String... contents) {
        setContents(Lists.newArrayList(contents));
    }

    @JsonIgnore
    public void setContents(List<String> contents) {
        if (contents == null) {
            this.content = null;
            this.contents = null;
            return;
        }

        switch (contents.size()) {
            case 0:
                this.content = null;
                this.contents = null;
                break;
            case 1:
                this.content = contents.get(0);
                this.contents = null;
                break;
            default:
                this.content = null;
                this.contents = contents;
        }
    }

    @JsonIgnore
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    @JsonIgnore
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @JsonIgnore
    public Date getLastModifiedDate() {
        return new Date(lastModifiedDate.getTime());
    }

    @JsonIgnore
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = new Date(lastModifiedDate.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranslationData)) return false;

        TranslationData that = (TranslationData) o;

        if (plural != that.plural) return false;
        if (revisionComment != null ?
                !revisionComment.equals(that.revisionComment) :
                that.revisionComment != null)
            return false;
        if (content != null ? !content.equals(that.content) :
            that.content != null)
            return false;
        if (contents != null ? !contents.equals(that.contents) :
            that.contents != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (revision != null ? !revision.equals(that.revision) :
            that.revision != null) return false;
        if (status != that.status) return false;
        if (lastModifiedBy != null
                ? !lastModifiedBy.equals(that.lastModifiedBy)
                : that.lastModifiedBy != null)
            return false;
        if (lastModifiedDate != null
                ? !lastModifiedDate.equals(that.lastModifiedDate)
                : that.lastModifiedDate != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        result = 31 * result +
                (revisionComment != null ? revisionComment.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (contents != null ? contents.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (plural ? 1 : 0);
        result = 31 * result +
                (lastModifiedBy != null? lastModifiedBy.hashCode() : 0);
        result = 31 * result +
                (lastModifiedDate != null ? lastModifiedDate.hashCode() : 0);
        return result;
    }
}
