package org.zanata.feature.account;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zanata.feature.account.comp.RegisterUsernameCharactersCTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
    ChangePasswordTest.class,
    InactiveUserLoginTest.class,
    ProfileTest.class,
    RegisterTest.class,
    UsernameValidationTest.class,
    EmailValidationTest.class,
    //Comprehensive tests
    RegisterUsernameCharactersCTest.class
})
public class AccountTestSuite {

}
