package org.zanata.search;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.zanata.common.HasContents;
import org.zanata.util.HqlCriterion;
import org.zanata.util.QueryBuilder;

import java.util.List;

/**
 * A helper class to build HQL condition for HTextFlow and HTextFlowTarget
 * contents match. Since we use content0...content6 to represent plurals it's
 * tedious to write a match condition manually. This class uses with...()
 * methods so that user can override number of
 * content fields (default is 6 and easier to see result in test if override to,
 * say, 2), case sensitivity and entity alias to suit particular use case.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ContentCriterion {
    // so that in test we can override and have less verbose string
    private final int numOfContentFields;
    private boolean caseSensitive;
    private String entityAlias;
    private final FilterConstraintToQuery.Parameters searchStringParam =
            FilterConstraintToQuery.Parameters.SearchString;

    public ContentCriterion() {
        this(HasContents.MAX_PLURALS);
    }

    @VisibleForTesting
    ContentCriterion(int numOfContentFields) {
        this.numOfContentFields = numOfContentFields;
    }

    @java.beans.ConstructorProperties({ "numOfContentFields", "caseSensitive",
            "entityAlias" })
    private ContentCriterion(int numOfContentFields, boolean caseSensitive,
            String entityAlias) {
        this.numOfContentFields = numOfContentFields;
        this.caseSensitive = caseSensitive;
        this.entityAlias = entityAlias;
    }

    public String contentsCriterionAsString() {
        String propertyAlias =
                Strings.isNullOrEmpty(entityAlias) ? "content" : entityAlias
                        + ".content";
        List<String> conditions = Lists.newArrayList();
        for (int i = 0; i < numOfContentFields; i++) {
            String contentFieldName = propertyAlias + i;
            conditions.add(HqlCriterion.like(contentFieldName,
                    this.caseSensitive, searchStringParam.placeHolder()));
        }
        return QueryBuilder.or(conditions);
    }

    ContentCriterion withNumOfContentFields(int numOfContentFields) {
        return this.numOfContentFields == numOfContentFields ? this :
                new ContentCriterion(numOfContentFields, this.caseSensitive,
                        this.entityAlias);
    }

    ContentCriterion withCaseSensitive(boolean caseSensitive) {
        return this.caseSensitive == caseSensitive ? this :
                new ContentCriterion(this.numOfContentFields, caseSensitive,
                        this.entityAlias);
    }

    ContentCriterion withEntityAlias(String entityAlias) {
        return this.entityAlias == entityAlias ? this :
                new ContentCriterion(this.numOfContentFields,
                        this.caseSensitive, entityAlias);
    }
}
