package org.zanata.action.validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.validation.ConstraintValidatorContext;

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
}
