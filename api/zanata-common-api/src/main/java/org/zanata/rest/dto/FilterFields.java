package org.zanata.rest.dto;

import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Label("Filter Constraints")
public class FilterFields implements Serializable {
    private static final long serialVersionUID = 1L;

    private String searchString;
    private String resId;
    private String changedBefore;
    private String changedAfter;
    private String lastModifiedByUser;
    private String sourceComment;
    private String transComment;
    private String msgContext;

    @DocumentationExample(value = "best of times", value2 = "besten Zeiten")
    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    @DocumentationExample(value = "521bbc0112de4af32209f11f07809614", value2 = "93453e83a6707092b76ebb960faf024e")
    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    @DocumentationExample(value = "2016-12-16", value2 = "2001-01-15")
    public String getChangedBefore() {
        return changedBefore;
    }

    public void setChangedBefore(String changedBefore) {
        this.changedBefore = changedBefore;
    }

    @DocumentationExample(value = "2004-03-02", value2 = "2014-11-12")
    public String getChangedAfter() {
        return changedAfter;
    }

    public void setChangedAfter(String changedAfter) {
        this.changedAfter = changedAfter;
    }

    @DocumentationExample(value = "damason", value2 = "jsmith")
    public String getLastModifiedByUser() {
        return lastModifiedByUser;
    }

    public void setLastModifiedByUser(String lastModifiedByUser) {
        this.lastModifiedByUser = lastModifiedByUser;
    }

    @DocumentationExample(value = "full name of the user", value2 = "product name")
    public String getSourceComment() {
        return sourceComment;
    }

    public void setSourceComment(String sourceComment) {
        this.sourceComment = sourceComment;
    }

    @DocumentationExample(value = "translated literally", value2 = "did not translate")
    public String getTransComment() {
        return transComment;
    }

    public void setTransComment(String transComment) {
        this.transComment = transComment;
    }

    @DocumentationExample(value = "main.c", value2 = "users.js")
    public String getMsgContext() {
        return msgContext;
    }

    public void setMsgContext(String msgContext) {
        this.msgContext = msgContext;
    }

}
