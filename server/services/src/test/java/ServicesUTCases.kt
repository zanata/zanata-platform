import com.googlecode.junittoolbox.SuiteClasses
import com.googlecode.junittoolbox.WildcardPatternSuite
import org.junit.runner.RunWith

/**
 * This JUnit Suite class provides a way of running all the unit tests
 * in 'services' from IntelliJ. To use it, just create an Arquillian Run
 * Configuration and choose this class in the Configuration tab
 * instead of an individual test.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(WildcardPatternSuite::class)
@SuiteClasses("**/*Test.class", "**/*Tests.class")
class ServicesUTCases
