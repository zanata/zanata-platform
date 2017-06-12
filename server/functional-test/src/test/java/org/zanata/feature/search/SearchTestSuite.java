package org.zanata.feature.search;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zanata.feature.search.comp.ExploreCTest;
import org.zanata.feature.search.comp.GroupSearchCTest;
import org.zanata.feature.search.comp.LanguageSearchCTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    PersonSearchTest.class,
    ProjectSearchTest.class,
    //comprehensive test
    ExploreCTest.class,
    GroupSearchCTest.class,
    LanguageSearchCTest.class
})
public class SearchTestSuite {
}
