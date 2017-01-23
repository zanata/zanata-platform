package org.zanata.dao;

import java.util.List;
import org.hibernate.transform.ResultTransformer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataJpaTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.TranslationMatrix;
import org.zanata.rest.service.StatisticsServiceImpl;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowBuilder;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HTextFlowTargetHistory;
import com.github.huangp.entityunit.entity.EntityMakerBuilder;
import com.github.huangp.entityunit.maker.FixedValueMaker;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import static org.assertj.core.api.Assertions.assertThat;

public class TextFlowTargetHistoryDAOTest extends ZanataJpaTest {

    private TextFlowTargetHistoryDAO historyDAO;
    private HPerson user;
    private HLocale hLocale;
    private DateTime today = new DateTime();
    private DateTime yesterday = new DateTime().minusDays(1);
    private DateTime twoDaysAgo = new DateTime().minusDays(2);
    private HDocument hDocument;
    private ResultTransformer resultTransformer;
    private static final DateTimeFormatter dateFormatter =
            DateTimeFormat.mediumDate();

    @Before
    public void setUp() throws Exception {
        resultTransformer =
                new StatisticsServiceImpl.UserMatrixResultTransformer(getEm(),
                        dateFormatter);
        historyDAO = new TextFlowTargetHistoryDAO(getSession()) {

            @Override
            protected String stripTimeFromDateTimeFunction(String columnName) {
                // we override mysql function with a h2 one
                return "formatdatetime(" + columnName + ", \'yyyy-MM-dd\')";
            }
        };
        deleteAllTables();
        hLocale = new HLocale(LocaleId.DE);
        getEm().persist(hLocale);
        user = makePerson("admin123");
        hDocument = EntityMakerBuilder.builder()
                .addFieldOrPropertyMaker(HProject.class, "sourceViewURL",
                        FixedValueMaker.EMPTY_STRING_MAKER)
                .build().makeAndPersist(getEm(), HDocument.class);
    }

    private HPerson makePerson(String username) {
        return EntityMakerBuilder.builder()
                .addFieldOrPropertyMaker(HAccount.class, "username",
                        FixedValueMaker.fix(username))
                .includeOptionalOneToOne().build()
                .makeAndPersist(getEm(), HPerson.class);
    }

    @Test
    public void canGetUserTranslationMatrix() {
        HTextFlowBuilder baseBuilder = new HTextFlowBuilder()
                .withDocument(hDocument).withTargetLocale(hLocale);
        HTextFlowBuilder modifiedYesterday = baseBuilder
                .withLastModifiedDate(yesterday).withLastModifiedBy(user);
        // make some text flows (all with 2 words) and some text flow targets
        // 1. one Approved target done yesterday
        modifiedYesterday.withResId("res1").withSourceContent("source 1")
                .withTargetContent("target 1")
                .withTargetState(ContentState.Approved).build();
        // 2. one Fuzzy target done yesterday
        HTextFlow res2 = modifiedYesterday.withResId("res2")
                .withSourceContent("source 2").withTargetContent("target 2")
                .withTargetState(ContentState.NeedReview).build();
        // 3. one Translated target done yesterday
        modifiedYesterday.withResId("res3").withSourceContent("source 3")
                .withTargetContent("target 3")
                .withTargetState(ContentState.Translated).build();
        HTextFlowBuilder modifiedTwoDaysAgo = baseBuilder
                .withLastModifiedDate(twoDaysAgo).withLastModifiedBy(user);
        // 4. one Fuzzy target done 2 days ago
        HTextFlow res4 = modifiedTwoDaysAgo.withResId("res4")
                .withSourceContent("source 4").withTargetContent("target 4")
                .withTargetState(ContentState.NeedReview).build();
        getEm().flush();
        // 5. one Fuzzy target history done yesterday
        HTextFlowTarget target2 = res2.getTargets().get(hLocale.getId());
        getEm().persist(new HTextFlowTargetHistory(target2));
        // 6. one Fuzzy target history done 2 days ago
        HTextFlowTarget target4 = res4.getTargets().get(hLocale.getId());
        getEm().persist(new HTextFlowTargetHistory(target4));
        // 7. one Translated target done today (which should not appear in
        // result)
        baseBuilder.withLastModifiedBy(user).withLastModifiedDate(today)
                .withResId("res7").withSourceContent("source 7")
                .withTargetContent("target 7")
                .withTargetState(ContentState.Translated).build();
        getEm().flush();
        List<TranslationMatrix> result = historyDAO.getUserTranslationMatrix(
                user, twoDaysAgo.withTimeAtStartOfDay(),
                today.withTimeAtStartOfDay(), Optional.<DateTimeZone> absent(),
                DateTimeZone.getDefault(), resultTransformer);
        assertThat(result).hasSize(4);
        final SavedDatePredicate yesterdayPredicate =
                new SavedDatePredicate(yesterday);
        Iterable<TranslationMatrix> yesterdayFuzzy =
                Iterables.filter(result, Predicates.and(yesterdayPredicate,
                        new ContentStatePredicate(ContentState.NeedReview)));
        assertThat(yesterdayFuzzy).describedAs("saved as fuzzy yesterday")
                .hasSize(1);
        assertThat(yesterdayFuzzy.iterator().next().getWordCount())
                .describedAs("total words saved as fuzzy yesterday")
                .isEqualTo(4);
        Iterable<TranslationMatrix> yesterdayApproved =
                Iterables.filter(result, Predicates.and(yesterdayPredicate,
                        new ContentStatePredicate(ContentState.Approved)));
        assertThat(yesterdayApproved).describedAs("saved as approved yesterday")
                .hasSize(1);
        assertThat(yesterdayApproved.iterator().next().getWordCount())
                .describedAs("total words saved as approved yesterday")
                .isEqualTo(2);
        Iterable<TranslationMatrix> yesterdayTranslated =
                Iterables.filter(result, Predicates.and(yesterdayPredicate,
                        new ContentStatePredicate(ContentState.Translated)));
        assertThat(yesterdayTranslated)
                .describedAs("saved as translated yesterday").hasSize(1);
        assertThat(yesterdayTranslated.iterator().next().getWordCount())
                .describedAs("total words saved as translated yesterday")
                .isEqualTo(2);
        Iterable<TranslationMatrix> twoDaysAgoFuzzy = Iterables.filter(result,
                Predicates.and(new SavedDatePredicate(twoDaysAgo),
                        new ContentStatePredicate(ContentState.NeedReview)));
        assertThat(twoDaysAgoFuzzy).describedAs("saved as fuzzy two days ago")
                .hasSize(1);
        assertThat(twoDaysAgoFuzzy.iterator().next().getWordCount())
                .describedAs("total words saved as fuzzy two days ago")
                .isEqualTo(4);
    }

    @Test
    public void whenSettingParameterIt() {
        HTextFlowBuilder baseBuilder = new HTextFlowBuilder()
                .withDocument(hDocument).withTargetLocale(hLocale);
        DateTimeZone zone = DateTimeZone.forID("Australia/Brisbane");
        baseBuilder.withLastModifiedDate(new DateTime(2015, 2, 1, 1, 0, zone))
                .withLastModifiedBy(user).withResId("res1")
                .withSourceContent("source 1").withTargetContent("target 1")
                .withTargetState(ContentState.Approved).build();
        getEm().flush();
        List<TranslationMatrix> result = historyDAO.getUserTranslationMatrix(
                user, new DateTime(2015, 2, 1, 1, 1, zone), new DateTime(zone),
                Optional.<DateTimeZone> absent(), DateTimeZone.getDefault(),
                resultTransformer);
        assertThat(result).isEmpty();
    }

    @Test
    public void canConvertTimeZoneIfUSerSuppliedDifferentZone() {
        String result = historyDAO.convertTimeZoneFunction("lastChanged",
                Optional.of(DateTimeZone.forID("Australia/Brisbane")),
                DateTimeZone.forID("America/Chicago"));
        assertThat(result).isEqualToIgnoringCase(
                "convert_tz(lastChanged, \'-06:00\', \'10:00\')");
    }

    private static class ContentStatePredicate
            implements Predicate<TranslationMatrix> {
        private final ContentState state;

        @Override
        public boolean apply(TranslationMatrix input) {
            return input.getSavedState() == state;
        }

        @java.beans.ConstructorProperties({ "state" })
        public ContentStatePredicate(final ContentState state) {
            this.state = state;
        }
    }

    private static class SavedDatePredicate
            implements Predicate<TranslationMatrix> {
        private final String theDate;

        private SavedDatePredicate(DateTime theDate) {
            this.theDate = dateFormatter.print(theDate.withTimeAtStartOfDay());
        }

        @Override
        public boolean apply(TranslationMatrix input) {
            return theDate.equals(input.getSavedDate());
        }
    }
}
