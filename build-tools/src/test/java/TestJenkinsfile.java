import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.MethodClosure;
import org.junit.Before;
import org.junit.Test;
import com.cloudbees.groovy.cps.impl.CpsCallableInvocation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lesfurets.jenkins.unit.cps.BasePipelineTestCPS;
import com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration;
import groovy.lang.Closure;

import static com.lesfurets.jenkins.unit.MethodSignature.method;
import static com.lesfurets.jenkins.unit.global.lib.GitSource.gitSource;
import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

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
                SH.INSTANCE);

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
        env.put("BRANCH_NAME", "master");
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
        steps.put("sh", SH.INSTANCE);
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
        getBinding().setProperty("manager",
                ImmutableMap.of("build",
                        ImmutableMap.of("project",
                                ImmutableMap.of())));
    }

    static class SH<T, R> extends Closure<R> {
        static final Closure INSTANCE = new SH();
        SH() {
            super(null);
        }

        @SuppressWarnings("unused")
        protected Object doCall(T args) {
            if (args instanceof String) return 0;

            @SuppressWarnings("unchecked")
            Map<String, ?> a = (Map<String, ?>) args;
            if (TRUE.equals(a.get("returnStdout"))) {
                String script = a.get("script").toString();
                if (script.endsWith("allocate-jboss-ports")) {
                    return "JBOSS_HTTP_PORT=51081\nSMTP_PORT=34765\n";
                }
                if (script.startsWith("git ls-remote")) {
// ScmGit.init in zanata-pipeline-library uses these:
                    if (script.endsWith("refs/pull/*/head")) {
                        return "1234567890123456789012345678901234567890 refs/pull/123/head\n" +
                                "6543516846846146541645265465464654264641 refs/pull/234/head";
                    } else if (script.endsWith("refs/heads/*")) {
                        return "fc2b7c527e4401c03bcaf2833739d16e77698ab6 refs/heads/master\n" +
                               "b0d3e2ff4696f2702f4b4fbac3b59b6cf9a76790 refs/heads/feature-branch";
                    } else if (script.contains("refs/tags/")) {
                        // TODO extract the requested tag and return it
                        return "b0d3e2ff4696f2702f4b4fbac3b59b6cf9a76790 refs/tags/v0.3.0\n" +
                               "5c612d80bfd7bf56cd97e8856fa2bed5f0d6e68f refs/tags/v0.3.1";
                    } else if (script.matches("refs/pull/.*/head")) {
                        return "b0d3e2ff4696f2702f4b4fbac3b59b6cf9a76790 refs/pull/123/head";
                    } else {
// Notifier.groovy in zanata-pipeline-library uses this:
                        return "fc2b7c527e4401c03bcaf2833739d16e77698ab6 refs/heads/master\n" +
                               "b0d3e2ff4696f2702f4b4fbac3b59b6cf9a76790 refs/heads/feature-branch\n" +
                               "1234567890123456789012345678901234567890 refs/tags/v0.1.0";
                    }
                }
            }
            if (TRUE.equals(a.get("returnStatus"))) {
                return 0;
            }
            return 0;
        }
    }

    @Test
    public void shouldExecuteWithoutErrors() throws Exception {
        try {
            // load and execute the Jenkinsfile
            runScript("../Jenkinsfile");
            printCallStack();
            assertJobStatusSuccess();

            boolean verified = getHelper().getCallStack()
                    .stream()
                    .filter(it -> it.getMethodName().equals("sh") && it.argsToString().contains("mvn"))
                    // snoop on mvn commands in the stream
//                    .peek(it -> System.out.printf("%s\n\n", it.argsToString()))
                    .anyMatch(it -> {
                        String args = it.argsToString();
                        return args.contains("-Dappserver=") && args.contains("install");
                    });
            assertThat(verified).isTrue();
        } catch (CpsCallableInvocation e) {
            // if the script fails, we need the call stack to tell us where the problem is
            // (CpsCallableInvocation tells us very little)
            System.err.println("CPS call stack:");
            getHelper().getCallStack().forEach(System.err::println);
            throw e;
        }
    }
}
