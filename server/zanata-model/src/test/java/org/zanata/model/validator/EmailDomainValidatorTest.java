package org.zanata.model.validator;

import java.util.Set;
import javax.inject.Provider;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class EmailDomainValidatorTest {

    @Mock
    private Provider<Set<String>> emailDomainsProvider;
    private EmailDomainValidator validator;
    @Mock
    private ConstraintValidatorContext context;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new EmailDomainValidator(emailDomainsProvider);
    }

    @Test
    public void nullOrBlankStringIsValid() {
        assertThat(validator.isValid(null, context)).isTrue();
        assertThat(validator.isValid("", context)).isTrue();
        assertThat(validator.isValid("  ", context)).isTrue();
    }

    @Test
    public void anyEmailIsvalidWhenNoRestrictionOnEmailDomain() {
        when(emailDomainsProvider.get()).thenReturn(emptySet());
        assertThat(validator.isValid(RandomStringUtils.randomAlphabetic(5), context)).isTrue();

    }

    @Test
    public void onlyMatchingEmailIsValid() {
        when(emailDomainsProvider.get()).thenReturn(newHashSet("redhat.com"));
        assertThat(validator.isValid("user@example.com", context)).isFalse();
        assertThat(validator.isValid("user@redhat.com", context)).isTrue();
    }
}
