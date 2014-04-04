package org.zanata.search;

import java.util.List;

import org.zanata.common.HasContents;
import org.zanata.util.HqlCriterion;
import org.zanata.util.QueryBuilder;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ContentCriterion {
    // so that in test we can override and have less verbose string
    private final int numOfContentFields;

    public ContentCriterion() {
        this(HasContents.MAX_PLURALS);
    }

    // test override-able
    ContentCriterion(int numOfContentFields) {
        this.numOfContentFields = numOfContentFields;
    }

    public String contentsCriterionAsString(String alias,
            boolean caseSensitive, String namedParam) {
        String propertyAlias =
                Strings.isNullOrEmpty(alias) ? "content" : alias + ".content";
        List<String> conditions = Lists.newArrayList();
        for (int i = 0; i < numOfContentFields; i++) {
            String contentFieldName = propertyAlias + i;
            conditions.add(HqlCriterion.like(contentFieldName, caseSensitive,
                    namedParam));
        }
        return QueryBuilder.or(conditions);
    }
}
