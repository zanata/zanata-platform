package org.zanata.service.mt.dto;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MTDocument {
    private String url;
    private List<TypeString> contents = Lists.newArrayList();
    private String localeCode;
    private String backendId;
    private List<APIResponse> warnings = Lists.newArrayList();

    public MTDocument(String url,
            List<TypeString> contents, String localeCode,
            String backendId) {
        this.url = url;
        this.contents = contents;
        this.localeCode = localeCode;
        this.backendId = backendId;
    }

    public MTDocument() {
    }

    public String getUrl() {
        return url;
    }

    public List<TypeString> getContents() {
        return contents;
    }

    public String getLocaleCode() {
        return localeCode;
    }

    public String getBackendId() {
        return backendId;
    }

    public List<APIResponse> getWarnings() {
        return warnings;
    }
}
