# Zanata Functional Tests

Zanata uses WebDriver and JUnit to execute functional tests.
The Zanata team tests on Chrome and ChromeDriver for CI, and as such recommend this for development.

Selenium WebDriver: http://www.seleniumhq.org/projects/webdriver/<br>
ChromeDriver: https://sites.google.com/a/chromium.org/chromedriver/

## QuickStart
To get up and running quickly with Zanata functional tests, Zanata can be built and run in a
JBoss/Wildfly cargo container, for running tests via an IDE.<br>
This can be achieved with the build script, using:<br>
`./build --all -w -Q -Dwebdriver.chromedriver=~/{path_to}/chromedriver -Dwebdriver.chrome.binary=/{path_to}/google-chrome`<br>
from the top level directory.<br>
When successful, a `[INFO] Press Ctrl-C to stop the container...` message will show to indicate the
container is running and ready to accept connections.<br>
If desired, the instance can be accessed from a browser at [http://localhost:8180/zanata]()

Alternatively, tests classes can be run from the command line with<br>
`mvn verify -Dfunctional-test -DskipArqTests -DskipUnitTests -Dinclude.test.patterns="**/*{TestFile}"`

## Structure

### Test Structure
The functional test package contains three distinct components:
- The Page
- The WorkFlow
- The Test

Each Page class, e.g. `RegisterPage.java`, contains a set of methods that attempt to closely resemble
the user's actions on a page, such as `enterName(String name)` or `pressSignUp()`.  Wherever possible,
these methods return the page object that makes sense after the action has occurred, so that these
functions can be easily chained, i.e. `enterName("aloy").enterPassword("4me2test").pressSignIn()`.<br>
Other page parts, such as specific notifications and messages, are good to place here.
In most cases a Page will extend the `CorePage`, providing access to global Zanata features such as the
sidebar or WebDriver basics such as `reload()`.

The WorkFlow classes, e.g. `LoginWorkFlow.java`, provide a convenient series of steps that perform a
recognised behaviour - such as `new LoginWorkFlow().signInAs("admin", "admin)`.  These are a good
way to start a test, placing the action in a predictable location without too many repetitive lines in
the test itself.

A test extends a basic test class `ZanataTestCase` that
- Starts a test with a clean environment
- Provides some basic users and projects
- Sets up a logging structure tied to the test

Each test class needs to annotated with @Category() - most likely for the DetailedTest.class - in order
for it to be run by Jenkins CI.

This is a basic Zanata functional test.<br>

```
...
@Category(DetailedTest.class)
public class RegisterTest extends ZanataTestCase {

    @Rule
    public final HasEmailRule emailRule = new HasEmailRule();

    Map<String, String> fields;
    private HomePage homePage;

    @BeforeEach
    public void before() {
        // fields contains a set of data that can be successfully registered
        fields = new HashMap<String, String>();

        // Conflicting fields - must be set for each test function to avoid
        // "not available" errors
        fields.put("email", "test@example.com");
        fields.put("username", "testusername");
        fields.put("name", "test");
        fields.put("password", "testpassword");
        homePage = new BasicWorkFlow().goToHome();
        homePage.deleteCookiesAndRefresh();
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void registerSuccessful() throws Exception {
        RegisterPage registerPage = homePage
                .goToRegistration()
                .setFields(fields);

        assertThat(registerPage.getErrors())
                .as("No errors are shown")
                .isEmpty();

        SignInPage signInPage = registerPage.register();

        assertThat(signInPage.getNotificationMessage())
                .isEqualTo(HomePage.SIGNUP_SUCCESS_MESSAGE)
                .as("Sign up is successful");
    }
}
```

### Rules
There are some @Rules that will aid in the creation of test cases. These are:<br>
- HasEmailRule: provides methods for interacting with system emails (as WiserMessages)
- CleanDocumentStorageRule: for cleaning out the document storage
- SampleProjectRule: creates and cleans project test data
- RetryRule: Override the default retry / flaky test system

### Suite Structure
The functional tests have been separated into suites, to aid customised execution.<br>

-<b>Detailed Tests</b><br>
This suite contains every standard functional test in the package, including the End To End tests.
Annotating a test class with `@Category(DetailedTest.class)` will add all tests to this category, or
single tests can be annotated if necessary.<br>
These tests are run after integration with the aim of quickly discovering errors that have slipped
through QE and marked as priority for the development team to address.

-<b>Basic Acceptance</b><br>
Basic Acceptance Tests are used as part of the review process and integration gate. Typically, the
end to end tests are included, as well as critical tests not covered by these.  A developer should
execute these tests before submitting their contribution to Github as a pull request.

-<b>Component Suites</b><br>
These suites contain a collection of tests that regard a particular area of Zanata, such as Account
and Documents, or a concept such as EndToEnd and Security.

-<b>Test Plan</b><br>
All suites are listed and interfaced through the `TestPlan` class collection, which provides some
documentation on each of the areas.
<br>
