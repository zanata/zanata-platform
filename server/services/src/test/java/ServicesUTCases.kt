import com.googlecode.junittoolbox.SuiteClasses
import com.googlecode.junittoolbox.WildcardPatternSuite
import org.junit.runner.RunWith

/**
 * This JUnit Suite class provides a way of running all the unit tests
 * in 'services' from IntelliJ (without picking up integration tests).
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(WildcardPatternSuite::class)
@SuiteClasses("**/*Test.class", "**/*Tests.class")
class ServicesUTCases
