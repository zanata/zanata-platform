package org.zanata.feature;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;

/**
 * Extend the full test suite, but filter by the Detailed Test category
 *
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@RunWith(Categories.class)
@Categories.IncludeCategory(DetailedTest.class)
public class DetailedTestSuite extends AggregateTestSuite {
}
