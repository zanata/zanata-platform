package org.zanata.feature.language;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zanata.feature.language.comp.LanguageCTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AddLanguageTest.class,
    ContactLanguageTeamTest.class,
    JoinLanguageTeamTest.class,
    //comprehensive test
    LanguageCTest.class

})
public class LanguageTestSuite {
}
