package org.zanata.feature.account;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
    ChangePasswordTest.class,
    InactiveUserLoginTest.class,
    ProfileTest.class,
    RegisterTest.class,
    UsernameValidationTest.class,
    EmailValidationTest.class
})
public class AccountTestSuite {

}
