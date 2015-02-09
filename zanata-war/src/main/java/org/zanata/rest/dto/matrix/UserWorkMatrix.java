package org.zanata.rest.dto.matrix;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Delegate;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.dao.TextFlowTargetHistoryDAO;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This will help us to build a nested map structure.
 *
 * @see org.zanata.rest.dto.matrix.DetailMatrix
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement
public class UserWorkMatrix implements Map<String, List<DetailMatrix>> {
    @Delegate
    private Map<String, List<DetailMatrix>> dateToProjects = Maps.newHashMap();

    @JsonIgnore
    public void putOrCreateIfAbsent(String date,
            TextFlowTargetHistoryDAO.UserTranslationMatrix matrixRecord) {
        DetailMatrix detailMatrix = new DetailMatrix(
                matrixRecord.getProjectIteration().getProject().getSlug(),
                matrixRecord.getProjectIteration().getSlug(),
                matrixRecord.getLocale().getLocaleId(),
                matrixRecord.getSavedState(), matrixRecord.getWordCount());
        if (containsKey(date)) {
            List<DetailMatrix> matrixList = get(date);
            matrixList.add(detailMatrix);
        } else {
            put(date, Lists.newArrayList(detailMatrix));
        }
    }
}
