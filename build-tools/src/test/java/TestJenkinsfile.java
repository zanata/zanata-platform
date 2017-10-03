import com.cloudbees.groovy.cps.impl.CpsCallableInvocation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lesfurets.jenkins.unit.cps.BasePipelineTestCPS;
import com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.MethodClosure;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lesfurets.jenkins.unit.MethodSignature.method;
import static com.lesfurets.jenkins.unit.global.lib.GitSource.gitSource;
import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library;
import static java.lang.Boolean.TRUE;

// try 'extends BasePipelineTest' for debugging in case of weird Groovy exceptions
public class TestJenkinsfile extends BasePipelineTestCPS {

    private static final String LIB_PATH = "target/libs";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        LibraryConfiguration library = library()
                .name("zanata-pipeline-library")
                .retriever(gitSource("https://github.com/zanata/zanata-pipeline-library"))
                // uncomment to use already-downloaded (perhaps modified) copy instead of git:
//                .retriever(localSource(LIB_PATH))
                .targetPath(LIB_PATH)
                .defaultVersion("master")
                .allowOverride(true)
                .implicit(false)
                .build();
        getHelper().registerSharedLibrary(library);

        // set up mock methods (Note: built-ins are in BasePipelineTest.setUp)
        getHelper().registerAllowedMethod("archive", ImmutableList.of(Map.class), null);
        getHelper().registerAllowedMethod("archive", ImmutableList.of(Object.class), null);
        getHelper().registerAllowedMethod("hipchatSend", ImmutableList.of(Map.class), null);
        getHelper().registerAllowedMethod("junit", ImmutableList.of(Map.class), null);
        getHelper().registerAllowedMethod("lock", ImmutableList.of(Map.class, Closure.class), null);
        getHelper().registerAllowedMethod("lock", ImmutableList.of(String.class, Closure.class), null);
        getHelper().registerAllowedMethod("milestone", ImmutableList.of(), null);
        getHelper().registerAllowedMethod("stash", ImmutableList.of(Map.class), null);
        getHelper().registerAllowedMethod("timestamps", ImmutableList.of(Closure.class), null);
        getHelper().registerAllowedMethod("unstash", ImmutableList.of(Map.class), null);
        getHelper().registerAllowedMethod("unstash", ImmutableList.of(String.class), null);
        getHelper().registerAllowedMethod("withEnv", ImmutableList.of(List.class, Closure.class), null);
        getHelper().registerAllowedMethod("wrap", ImmutableList.of(Map.class, Closure.class), null);

        getHelper().registerAllowedMethod(method("findFiles", Map.class), args -> {
            String glob = ((Map)args).get("glob").toString();
            if (glob.equals("**/target/surefire-reports/TEST-*.xml")) {
                return new File[]{new File("server/zanata-war/target/surefire-reports/TEST-org.zanata.xml.StreamSerializerTest.xml")};
            }
            if (glob.equals("**/target/failsafe-reports/TEST-*.xml")) {
                return new File[]{new File("server/functional-test/target/failsafe-reports/TEST-org.zanata.feature.testharness.BasicAcceptanceTestSuite.xml")};
            }
            throw new RuntimeException("Unmocked invocation");
        });
        getHelper().registerAllowedMethod(method("sh", Map.class),
                args -> {
                    @SuppressWarnings("unchecked")
                    Map<String, ?> a = (Map<String, ?>) args;
                    if (TRUE.equals(a.get("returnStdout"))) {
                        String script = a.get("script").toString();
                        if (script.contains("allocate-jboss-ports")) {
                            return "JBOSS_HTTP_PORT=51081\nSMTP_PORT=34765\n";
                        }
                        // Notifier.groovy in zanata-pipeline-library uses this:
                        if (script.contains("git ls-remote")) {
                            return "1234567890 abcdef\n";
                        }
                    }
                    if (TRUE.equals(a.get("returnStatus"))) {
                        return 0;
                    }
                    return 0;
                });
        // PipelineUnit(withCredentialsInterceptor) can't handle a List<Map>
        // TODO for some reason the steps inside closure.call() are not shown as nested
        getHelper().registerAllowedMethod("withCredentials",
                ImmutableList.of(List.class, Closure.class),
                new Closure(null) {
                    @Override
                    public Object call(Object... args) {
                        Closure closure = (Closure) args[1];
                        return closure.call();
                    }
                });

        // environment variables
        Map<String, String> env = new HashMap<>();
        env.put("BUILD_URL", "http://example.com/job/JobName/123");
        env.put("JOB_NAME", "JobName");
        env.put("BRANCH_NAME", "PR-456");
        env.put("BUILD_NUMBER", "123");
        env.put("EXECUTOR_NUMBER", "1");
        env.put("DEFAULT_NODE", "master");
        env.put("NODE_NAME", "jenkins-pipeline-unit");

        // these steps will be passed by reference to library methods
        Map<String, Closure> steps = new HashMap<>();
        steps.put("hipchatSend", Closure.IDENTITY);
        steps.put("echo", Closure.IDENTITY);
        steps.put("emailext", Closure.IDENTITY);
        steps.put("emailextrecipients", Closure.IDENTITY);
        steps.put("library", Closure.IDENTITY);
        steps.put("sh", Closure.IDENTITY);
        steps.put("step", Closure.IDENTITY);
        // we need this for CPS mode
        MethodClosure.ALLOW_RESOLVE = true;

        // global variables
        getBinding().setProperty("env", env);
        getBinding().setProperty("steps", steps);
        getBinding().setProperty("params", ImmutableMap.of("LABEL", "master"));
        getBinding().setProperty("LABEL", "master");

        // these objects are just used as parameters
        getBinding().setProperty("scm", ImmutableMap.of());
        getBinding().setProperty("manager", ImmutableMap.of());
    }

    @Test
    public void shouldExecuteWithoutErrors() throws Exception {

        try {
            // load and execute the Jenkinsfile
            loadScript("../Jenkinsfile");
            printCallStack();
            assertJobStatusSuccess();
            // TODO add assertions about call stack (but not too fragile)
        } catch (CpsCallableInvocation e) {
            // if the script fails, we need the call stack to tell us where the problem is
            // (CpsCallableInvocation tells us very little)
            System.err.println("CPS call stack:");
            getHelper().getCallStack().forEach(System.err::println);
            throw e;
        }
    }
}
