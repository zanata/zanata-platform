package org.zanata.webtrans.shared.model;

import java.util.Date;

import org.zanata.common.LocaleId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GlossaryDetails implements IsSerializable {
    private Long id;
    private String description;
    private String pos;
    private String targetComment;
    private String sourceRef;
    private LocaleId srcLocale;
    private LocaleId targetLocale;
    private Integer targetVersionNum;
    private String source;
    private String target;
    private String url;
    private Date lastModifiedDate;

    @SuppressWarnings("unused")
    private GlossaryDetails() {
        this(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public GlossaryDetails(Long id, String source, String target,
            String description, String pos, String targetComment,
            String sourceRef, LocaleId srcLocale, LocaleId targetLocale,
            String url, Integer targetVersionNum, Date lastModifiedDate) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.description = description;
        this.pos = pos;
        this.targetComment = targetComment;
        this.sourceRef = sourceRef;
        this.srcLocale = srcLocale;
        this.targetLocale = targetLocale;
        this.url = url;
        this.targetVersionNum = targetVersionNum;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getPos() {
        return pos;
    }

    public String getTargetComment() {
        return targetComment;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public LocaleId getSrcLocale() {
        return srcLocale;
    }

    public LocaleId getTargetLocale() {
        return targetLocale;
    }

    public Integer getTargetVersionNum() {
        return targetVersionNum;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getUrl() {
        return url;
    }
}
