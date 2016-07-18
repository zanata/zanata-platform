package org.zanata.action.validator;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HPerson;
import org.zanata.test.CdiUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(CdiUnitRunner.class)
public class DuplicateEmailValidatorTest {

    @Inject
    private DuplicateEmailValidator validator;
    @Produces @Mock
    private PersonDAO personDAO;
    private ConstraintValidatorContext context = null;

    @Test
    public void nullOrEmptyStringIsValid() throws Exception {
        assertThat(validator.isValid(null, context)).isTrue();
        assertThat(validator.isValid("", context)).isTrue();
    }

    @Test
    public void notEmptyStringIsValidIfThereIsNoPersonWithThisEmail() {
        when(personDAO.findByEmail("a@b.c")).thenReturn(null);
        Assertions.assertThat(validator.isValid("a@b.c", context)).isTrue();
    }

    @Test
    public void notEmptyStringIsInvalidIfThereIsPersonWithThisEmail() {
        when(personDAO.findByEmail("a@b.c")).thenReturn(new HPerson());
        Assertions.assertThat(validator.isValid("a@b.c", context)).isFalse();
    }
}
