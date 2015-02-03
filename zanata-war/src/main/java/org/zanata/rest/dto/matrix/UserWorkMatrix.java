package org.zanata.rest.dto.matrix;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Delegate;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.model.UserTranslationMatrix;

import com.google.common.collect.Maps;

/**
 * This will help us to build a nested map structure. e.g.
 *
 * <pre>
 *  {
 *      "2015-01-03": {
 *          "plurals": {
 *              "master": {
 *                  "pl": [
 *                      { "contentState": "Translated", "wordCount": 7 }
 *                   ]
 *              }
 *          }
 *      },
 *      "2015-02-01": {
 *          "plurals": {
 *              "master": {
 *                  "pl": [
 *                      { "contentState": "Translated", "wordCount": 13 },
 *                      { "contentState": "NeedReview", "wordCount": 7 }
 *                   ]
 *              }
 *          }
 *      }
 *  }
 * </pre>
 *
 * @see org.zanata.rest.dto.matrix.PerProjectMatrix
 * @see org.zanata.rest.dto.matrix.PerProjectVersionMatrix
 * @see org.zanata.rest.dto.matrix.PerLocaleMatrix
 * @see org.zanata.rest.dto.matrix.ContentStateToWordCount
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement
public class UserWorkMatrix implements Map<String, PerProjectMatrix>,
        MatrixMap<String> {
    @Delegate
    private Map<String, PerProjectMatrix> dateToProjects = Maps.newHashMap();

    @JsonIgnore
    @Override
    public void putOrCreateIfAbsent(String date,
            UserTranslationMatrix matrixRecord) {
        if (containsKey(date)) {
            PerProjectMatrix perProjectMatrix = get(date);
            perProjectMatrix.putOrCreateIfAbsent(
                    matrixRecord.getProjectIteration().getProject().getSlug(),
                    matrixRecord);
        } else {
            put(date, new PerProjectMatrix(matrixRecord));
        }
    }
}
