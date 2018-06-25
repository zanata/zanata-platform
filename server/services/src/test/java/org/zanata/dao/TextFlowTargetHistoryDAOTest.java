package org.zanata.dao;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.ZanataJpaTest;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.TranslationMatrix;
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
import org.zanata.rest.service.StatisticsServiceImpl.UserMatrixResultTransformer;
import org.zanata.security.ZanataIdentity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.common.ContentState.*;

public class TextFlowTargetHistoryDAOTest extends ZanataJpaTest {

    private TextFlowTargetHistoryDAO historyDAO;
    private ZanataIdentity identity;
    private HPerson user;
    private HLocale hLocale;
    private DateTime today = new DateTime();
    private DateTime yesterday = new DateTime().minusDays(1);
    private DateTime twoDaysAgo = new DateTime().minusDays(2);
    private HDocument hDocument;
    private UserMatrixResultTransformer resultTransformer;
    private static final DateTimeFormatter dateFormatter =
            DateTimeFormat.mediumDate();

    @Before
    public void setUp() throws Exception {
        identity = Mockito.mock(ZanataIdentity.class);
        resultTransformer =
                new UserMatrixResultTransformer(getEm(),
                        identity, dateFormatter);
        historyDAO = new TextFlowTargetHistoryDAO(getSession()) {

            private static final long serialVersionUID = 1L;

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
        HAccount hAccount = new HAccount();
        hAccount.setUsername(username);
        getEm().persist(hAccount);
        return EntityMakerBuilder.builder()
                .reuseEntity(hAccount)
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
                .withTargetState(Approved).build();
        // 2. one Fuzzy target done yesterday
        HTextFlow res2 = modifiedYesterday.withResId("res2")
                .withSourceContent("source 2").withTargetContent("target 2")
                .withTargetState(NeedReview).build();
        // 3. one Translated target done yesterday
        modifiedYesterday.withResId("res3").withSourceContent("source 3")
                .withTargetContent("target 3")
                .withTargetState(Translated).build();
        HTextFlowBuilder modifiedTwoDaysAgo = baseBuilder
                .withLastModifiedDate(twoDaysAgo).withLastModifiedBy(user);
        // 4. one Fuzzy target done 2 days ago
        HTextFlow res4 = modifiedTwoDaysAgo.withResId("res4")
                .withSourceContent("source 4").withTargetContent("target 4")
                .withTargetState(NeedReview).build();
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
                .withTargetState(Translated).build();
        getEm().flush();
        List<TranslationMatrix> result = historyDAO.getUserTranslationMatrix(
                user, twoDaysAgo.withTimeAtStartOfDay(),
                today.withTimeAtStartOfDay(), Optional.<DateTimeZone> absent(),
                DateTimeZone.getDefault(), resultTransformer);
        assertThat(result).hasSize(4);
        final SavedDatePredicate yesterdayPredicate =
                new SavedDatePredicate(yesterday);
        List<TranslationMatrix> yesterdayFuzzy = result.stream()
                .filter(yesterdayPredicate.and(savedState(NeedReview)))
                .collect(Collectors.toList());
        assertThat(yesterdayFuzzy).describedAs("saved as fuzzy yesterday")
                .hasSize(1);
        assertThat(yesterdayFuzzy.get(0).getWordCount())
                .describedAs("total words saved as fuzzy yesterday")
                .isEqualTo(4);
        List<TranslationMatrix> yesterdayApproved = result.stream()
                .filter(yesterdayPredicate.and(savedState(Approved)))
                .collect(Collectors.toList());
        assertThat(yesterdayApproved).describedAs("saved as approved yesterday")
                .hasSize(1);
        assertThat(yesterdayApproved.get(0).getWordCount())
                .describedAs("total words saved as approved yesterday")
                .isEqualTo(2);
        List<TranslationMatrix> yesterdayTranslated = result.stream()
                .filter(yesterdayPredicate.and(savedState(Translated)))
                .collect(Collectors.toList());
        assertThat(yesterdayTranslated)
                .describedAs("saved as translated yesterday").hasSize(1);
        assertThat(yesterdayTranslated.get(0).getWordCount())
                .describedAs("total words saved as translated yesterday")
                .isEqualTo(2);
        List<TranslationMatrix> twoDaysAgoFuzzy = result.stream()
                .filter(
                new SavedDatePredicate(twoDaysAgo).and(savedState(NeedReview)))
                .collect(Collectors.toList());
        assertThat(twoDaysAgoFuzzy).describedAs("saved as fuzzy two days ago")
                .hasSize(1);
        assertThat(twoDaysAgoFuzzy.get(0).getWordCount())
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
                .withTargetState(Approved).build();
        getEm().flush();
        List<TranslationMatrix> result = historyDAO.getUserTranslationMatrix(
                user, new DateTime(2015, 2, 1, 1, 1, zone), new DateTime(zone),
                Optional.absent(), DateTimeZone.getDefault(),
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

    private Predicate<TranslationMatrix> savedState(ContentState state) {
        return it -> it.getSavedState() == state;
    }

    private static class SavedDatePredicate
            implements Predicate<TranslationMatrix> {
        private final String theDate;

        private SavedDatePredicate(DateTime theDate) {
            this.theDate = dateFormatter.print(theDate.withTimeAtStartOfDay());
        }

        @Override
        public boolean test(TranslationMatrix input) {
            return theDate.equals(input.getSavedDate());
        }
    }
}
