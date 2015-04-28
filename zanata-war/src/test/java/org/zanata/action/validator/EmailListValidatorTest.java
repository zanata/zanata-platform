package org.zanata.action.validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

@Test(groups = { "unit-tests" })
public class EmailListValidatorTest {

    private EmailListValidator validator;
    private ConstraintValidatorContext context = null;

    @BeforeMethod
    public void create() {
        validator = new EmailListValidator();
        validator.initialize(null);
    }

    @Test
    public void nullIsValid() throws Exception {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    public void emptyIsValid() throws Exception {
        assertTrue(validator.isValid("", context));
    }

    @Test
    public void aIsValid() throws Exception {
        assertTrue(validator.isValid(" a@example.com ", context));
    }

    @Test
    public void aBIsValid() throws Exception {
        assertTrue(validator.isValid("a@example.com, b@example.org", context));
    }

    @Test
    public void CIsInvalid() throws Exception {
        assertFalse(validator.isValid("c", context));
    }

    @Test
    public void DIsValid() throws Exception {
        assertTrue(validator.isValid("d@example", context));
    }

    @Test
    public void aBCDIsValid() throws Exception {
        assertFalse(validator
                .isValid("a@example.com, b@example.org c d@example", context));
    }

    @Test
    public void nullOrEmptyStringIsValid() throws Exception {
        assertThat(validator.isValid(null, context)).isTrue();
        assertThat(validator.isValid("", context)).isTrue();
    }

    @Test
    public void isValidIfListsAreEmailsSeparatedByComma() {
        assertThat(validator.isValid("a@b.co,a@c.co", context)).isTrue();
        assertThat(validator.isValid("a@b.co ,a@c.co", context)).isTrue();
        assertThat(validator.isValid("a@b.co, a@c.co", context)).isTrue();
        assertThat(validator.isValid("a@b.co , a@c.co", context)).isTrue();
        assertThat(validator.isValid(" a@b.co , a@c.co", context)).isTrue();
        assertThat(validator.isValid(" a@b.co , a@c.co ", context)).isTrue();
    }

    @Test
    public void isInvalidIfListsContainNotValidEmail() {
        assertThat(validator.isValid("a@b.co,a b@1", context)).isFalse();
        assertThat(validator.isValid("a@b.co ,d", context)).isFalse();
    }
}
