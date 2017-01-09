package org.zanata.client;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommands;
import org.zanata.client.commands.AppAbortException;
import org.zanata.client.commands.AppAbortStrategy;
import org.zanata.client.commands.ArgsUtil;
import org.zanata.client.commands.BasicOptions;
import org.zanata.client.commands.BasicOptionsImpl;
import org.zanata.client.commands.ListRemoteOptionsImpl;
import org.zanata.client.commands.PutProjectOptionsImpl;
import org.zanata.client.commands.PutUserOptionsImpl;
import org.zanata.client.commands.PutVersionOptionsImpl;
import org.zanata.client.commands.SystemExitStrategy;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.commands.glossary.delete.GlossaryDeleteOptionsImpl;
import org.zanata.client.commands.glossary.pull.GlossaryPullOptionsImpl;
import org.zanata.client.commands.glossary.push.GlossaryPushOptionsImpl;
import org.zanata.client.commands.init.InitOptionsImpl;
import org.zanata.client.commands.pull.PullOptionsImpl;
import org.zanata.client.commands.push.PushOptionsImpl;
import org.zanata.client.commands.stats.GetStatisticsOptionsImpl;
import org.zanata.util.VersionUtility;

import com.google.common.collect.ImmutableMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class ZanataClient extends BasicOptionsImpl {
    public static final String COMMAND_NAME = System.getProperty("app.name",
            "zanata-cli");
    public static final String COMMAND_DESCRIPTION =
            "Zanata Java command-line client";

    private boolean version;
    private final CmdLineParser parser = new CmdLineParser(this);
    private final AppAbortStrategy abortStrategy;
    private final PrintWriter out;
    private final PrintWriter err;

    @Argument(handler = SubCommandHandler2.class, metaVar = "<command>")
    @SubCommands({
            @SubCommand(name = "help", impl = HelpOptions.class),
            @SubCommand(name = "init", impl = InitOptionsImpl.class),
            // @SubCommand(name="list-local", impl=ListLocalOptionsImpl.class),
            @SubCommand(name = "list-remote",
                    impl = ListRemoteOptionsImpl.class),
            @SubCommand(name = "pull", impl = PullOptionsImpl.class),
            @SubCommand(name = "push", impl = PushOptionsImpl.class),
            @SubCommand(name = "put-project",
                    impl = PutProjectOptionsImpl.class),
            @SubCommand(name = "put-user", impl = PutUserOptionsImpl.class),
            @SubCommand(name = "put-version",
                    impl = PutVersionOptionsImpl.class),
            @SubCommand(name = "stats", impl = GetStatisticsOptionsImpl.class),
            @SubCommand(name = "glossary-delete", impl = GlossaryDeleteOptionsImpl.class),
            @SubCommand(name = "glossary-push", impl = GlossaryPushOptionsImpl.class),
            @SubCommand(name = "glossary-pull", impl = GlossaryPullOptionsImpl.class)})

    // if this field name changes, change COMMAND_FIELD too
    private Object command;
    private static final String COMMAND_FIELD = "command";
    public static final ImmutableMap<String, Class<BasicOptions>> OPTIONS;

    static {
        try {
            ImmutableMap.Builder<String, Class<BasicOptions>> m =
                    ImmutableMap.builder();

            Field cmdField = ZanataClient.class.getDeclaredField(COMMAND_FIELD);
            SubCommands subCommands = cmdField.getAnnotation(SubCommands.class);
            for (SubCommand sub : subCommands.value()) {
                if (BasicOptions.class.isAssignableFrom(sub.impl())) {
                    Class<BasicOptions> clazz =
                            (Class<BasicOptions>) sub.impl();
                    m.put(sub.name(), clazz);
                }
            }
            OPTIONS = m.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        ZanataClient tool = new ZanataClient();
        tool.processArgs(args);
    }

    @Override
    public ZanataCommand initCommand() {
        return null;
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    ZanataClient() {
        this(new SystemExitStrategy(), new PrintWriter(System.out),
                new PrintWriter(System.err));
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    @Deprecated
    public ZanataClient(AppAbortStrategy strategy, PrintStream out,
            PrintStream err) {
        this(strategy, new PrintWriter(new OutputStreamWriter(out)),
                new PrintWriter(new OutputStreamWriter(err)));
    }

    public ZanataClient(AppAbortStrategy strategy, Writer out,
            Writer err) {
        this(strategy, new PrintWriter(out), new PrintWriter(err));
    }

    public ZanataClient(AppAbortStrategy strategy, PrintWriter out,
            PrintWriter err) {
        this.abortStrategy = strategy;
        this.out = out;
        this.err = err;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getCommandDescription() {
        return COMMAND_DESCRIPTION;
    }

    protected void processArgs(String... args) {
        try {
            if (args.length == 2 && OPTIONS.containsKey(args[0])
                    && (args[1].equals("--help") || args[1].equals("-h"))) {
                String cmdName = args[0];
                BasicOptions opts = OPTIONS.get(cmdName).newInstance();
                new ArgsUtil(abortStrategy, opts).printHelp(out,
                        getCommandName());
                return;
            }
            parser.parseArgument(args);
            if (version) {
                out.println(getCommandName());
                VersionUtility.printVersions(ZanataClient.class, out);
            } else if (getHelp() || command == null) {
                printHelp(out);
            } else if (command instanceof HelpOptions) {
                HelpOptions helpCmd = (HelpOptions) command;
                if (helpCmd.getCommand() == null) {
                    // generic help
                    printHelp(out);
                } else if (!OPTIONS.containsKey(helpCmd.getCommand())) {
                    // sub-command not recognised
                    out.println("Error: Unknown command '" + helpCmd.getCommand() + "'");
                    printHelp(out);
                } else {
                    // help for a sub-command
                    String cmdName = helpCmd.getCommand();
                    BasicOptions opts = OPTIONS.get(cmdName).newInstance();
                    new ArgsUtil(abortStrategy, opts).printHelp(out,
                            getCommandName());
                }
            } else if (command instanceof BasicOptions) {
                BasicOptions opts = (BasicOptions) command;
                copyGlobalOptionsTo(opts);
                new ArgsUtil(abortStrategy, opts).runCommand();
            } else {
                throw new RuntimeException("unexpected command type");
            }
        } catch (CmdLineException e) {
            String msg = e.getMessage();
            err.println(msg);
            printHelp(err);
            abortStrategy.abort(msg);
        } catch (AppAbortException e) {
            throw e;
        } catch (Exception e) {
            ArgsUtil.handleException(e, getErrors(), abortStrategy);
        } finally {
            out.flush();
            err.flush();
        }
    }

    /**
     * @param options
     */
    private void copyGlobalOptionsTo(BasicOptions options) {
        options.setDebug(getDebug());
        options.setErrors(getErrors());
        options.setHelp(getHelp());
        options.setInteractiveMode(isInteractiveMode());
        options.setQuiet(getQuiet());
    }

    private void printHelp(PrintWriter out) {
        out.print("Usage: " + getCommandName());
        parser.printSingleLineUsage(out, null);
        out.println();
        out.println();
        out.println(getCommandDescription());
        out.println();
        parser.printUsage(out, null);
        out.println();
        out.println("Type '" + getCommandName()
                + " help <command>' for help on a specific command.");
        out.println();
        out.println("Available commands:");
        for (String cmd : OPTIONS.keySet()) {
            out.println("  " + cmd);
            // + ": " + OPTIONS.get(cmd).newInstance().
            // getCommandDescription()
        }
    }

    @Option(name = "--version", aliases = { "-v" },
            usage = "Output version information and exit")
    public void setVersion(boolean version) {
        this.version = version;
    }

}
