package org.zanata.rest.dto.stats.contribution;

import org.hamcrest.core.StringContains;
import org.junit.Test;
import org.zanata.common.BaseTranslationCount;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.DTOUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ContributionStatisticsTest {

    @Test
    public void objectToJSONTest() {
        ContributionStatistics stats = generateObject();
        String result = DTOUtil.toJSON(stats);

        assertThat(result, not(isEmptyOrNullString()));
        assertThat(result, StringContains.containsString("user1"));
        assertThat(result,
                StringContains.containsString(LocaleId.DE.toString()));
        assertThat(result, StringContains.containsString("10"));
    }

    @Test
    public void JSONToObjectTest() {
        ContributionStatistics originalObj = generateObject();
        String json = DTOUtil.toJSON(originalObj);

        ContributionStatistics marshallObj =
                DTOUtil.fromJSONToObject(json, ContributionStatistics.class);

        assertThat(marshallObj, notNullValue());
        assertThat(marshallObj, equalTo(originalObj));
    }

    private ContributionStatistics generateObject() {
        BaseContributionStatistic data =
                new BaseContributionStatistic(40, 20, 10, 50);
        LocaleStatistics localeStatistics = new LocaleStatistics();
        localeStatistics.put(LocaleId.DE, data);

        ContributionStatistics statistics = new ContributionStatistics();
        statistics.put("user1", localeStatistics);

        return statistics;
    }
}
