package org.zanata.rest.editor.dto;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.rest.search.dto.LanguageTeamSearchResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@AllArgsConstructor
public class LocalesResults implements Serializable {
    @Getter
    public int totalCount;

    @Getter
    public List<LanguageTeamSearchResult> results;
}
