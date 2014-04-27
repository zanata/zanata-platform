package org.zanata.search;

import java.util.List;

import org.zanata.common.HasContents;
import org.zanata.util.HqlCriterion;
import org.zanata.util.QueryBuilder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Wither(AccessLevel.PACKAGE)
public class ContentCriterion {
    // so that in test we can override and have less verbose string
    private final int numOfContentFields;
    private boolean caseSensitive;
    private String entityAlias;
    private FilterConstraintToQuery.Parameters searchStringPram =
            FilterConstraintToQuery.Parameters.searchString;

    public ContentCriterion() {
        this(HasContents.MAX_PLURALS);
    }

    @VisibleForTesting
    ContentCriterion(int numOfContentFields) {
        this.numOfContentFields = numOfContentFields;
    }

    public String contentsCriterionAsString() {
        String propertyAlias =
                Strings.isNullOrEmpty(entityAlias) ? "content" : entityAlias + ".content";
        List<String> conditions = Lists.newArrayList();
        for (int i = 0; i < numOfContentFields; i++) {
            String contentFieldName = propertyAlias + i;
            conditions.add(HqlCriterion.like(contentFieldName, this.caseSensitive,
                    searchStringPram.placeHolder()));
        }
        return QueryBuilder.or(conditions);
    }
}
