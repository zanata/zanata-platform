package org.zanata.action.validator;

import javax.validation.ConstraintValidatorContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainListValidatorTest {

    private DomainListValidator validator;
    @Mock private ConstraintValidatorContext context;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new DomainListValidator();
    }

    @Test
    public void nullOrBlankIsValid() {
        assertThat(validator.isValid(null, context)).isTrue();
        assertThat(validator.isValid("", context)).isTrue();
        assertThat(validator.isValid(" ", context)).isTrue();
    }

    @Test
    public void singleDomainIsValid() {
        assertThat(validator.isValid("example.com", context)).isTrue();
    }

    @Test
    public void multipleDomainsSeparatedByCommaIsValid() {
        assertThat(validator.isValid("example.com,redhat.com", context)).isTrue();
        assertThat(validator.isValid("example.com, redhat.com", context)).isTrue();
        assertThat(validator.isValid("example.com ,redhat.com", context)).isTrue();
        assertThat(validator.isValid("example.com , redhat.com", context)).isTrue();
        assertThat(validator.isValid("example.com   ,   redhat.com", context)).isTrue();

    }

    @Test
    public void nonDomainIsInvalid() {
        // looks like EmailValidator accepts anything with a @ sign
//        System.out.println(new EmailValidator().isValid("a@1", null));
        assertThat(validator.isValid("@", context)).isFalse();
        assertThat(validator.isValid("example.com,@", context)).isFalse();
    }

}
