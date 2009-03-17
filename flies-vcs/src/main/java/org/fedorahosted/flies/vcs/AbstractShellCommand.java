package org.fedorahosted.flies.vcs;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AbstractShellCommand {
    /**
     * 
     */
    public static final int DEFAULT_TIMEOUT = 120000;

    private class InputStreamConsumer extends Thread {
        private byte[] output;
        private final InputStream stream;

        public InputStreamConsumer(InputStream stream) {
            this.stream = new BufferedInputStream(stream);
        }

        public byte[] getBytes() {
            return output;
        }

        @Override
        public void run() {
            ByteArrayOutputStream myOutput = new ByteArrayOutputStream();
            try {
                int length;
                byte[] buffer = new byte[1024];

                while ((length = stream.read(buffer)) != -1) {
                    myOutput.write(buffer, 0, length);
                }
                this.output = myOutput.toByteArray();
            } catch (IOException e) {
                if (!interrupted()) {
                    
                }
            } finally {
                try {
                    this.stream.close();
                } catch (IOException e) {
                    
                }
                try {
                    myOutput.close();
                } catch (IOException e) {
                   
                }
            }
        }

    }

    public static final int MAX_PARAMS = 120;
    protected String command;
    protected List<String> commands;
    protected boolean escapeFiles;
    protected List<String> options = new ArrayList<String>();
    protected File workingDir;
    final List<String> files = new ArrayList<String>();

    private InputStreamConsumer consumer;
    private Process process;

    protected AbstractShellCommand() {
        
    }

    public AbstractShellCommand(List<String> commands, File workingDir,
            boolean escapeFiles) {
        this();
        this.command = null;
        this.escapeFiles = escapeFiles;
        this.workingDir = workingDir;
        this.commands = commands;
    }

    public void addOptions(String... optionsToAdd) {
        for (String option : optionsToAdd) {
            this.options.add(option);
        }
    }

    public byte[] executeToBytes()  {
        int timeout = DEFAULT_TIMEOUT;
        return executeToBytes(timeout);
    }

    public byte[] executeToBytes(int timeout) {
        return this.executeToBytes(timeout, true);
    }

    /**
     * Execute a command.
     * 
     * @param timeout
     *            -1 if no timeout, else the timeout in ms.
     * @return
     * @throws HgException
     */
    public byte[] executeToBytes(int timeout, boolean expectPositiveReturnValue) {
        try {
            List<String> cmd = getCommands();
            String cmdString = cmd.toString().replace(",", "").substring(1);
            cmdString = cmdString.substring(0, cmdString.length() - 1);

            ProcessBuilder builder = new ProcessBuilder(cmd);
            
            // set locale to english have deterministic output
            Map<String, String> env = builder.environment();
            env.put("LC_ALL", "en_US.utf8");
            env.put("LANG", "en_US.utf8");
            env.put("LANGUAGE", "en_US.utf8");
            env.put("LC_MESSAGES", "en_US.utf8");
            
            
            builder.redirectErrorStream(true); // makes my life easier
            if (workingDir != null) {
                builder.directory(workingDir);
            }
            process = builder.start();
            consumer = new InputStreamConsumer(process.getInputStream());
            byte[] returnValue = null;
            consumer.start();
            consumer.join(timeout); // 30 seconds timeout
            if (!consumer.isAlive()) {
                int exitCode = process.waitFor();
                returnValue = consumer.getBytes();
                // everything fine
                if (exitCode == 0 || !expectPositiveReturnValue) {
                    return returnValue;
                }
                // exit code > 0
                // exit code == 1 usually isn't fatal.
            }
        } catch (IOException e) {
        } catch (InterruptedException e) {
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
		return null;
    }

    public String executeToString() {
        byte[] bytes = executeToBytes();
        if (bytes != null) {
            return new String(bytes);
        }
        return "";
    }
    
    public void addFiles(Collection<String> myFiles) {
        for (String file : myFiles) {
            this.files.add(file);
        }
    }

    protected List<String> getCommands() {
        if (commands != null) {
            return commands;
        }
        ArrayList<String> result = new ArrayList<String>();
        result.add(getExecutable());
        result.add(command);
        result.addAll(options);
        if (escapeFiles && !files.isEmpty()) {
            result.add("--");
        }
        result.addAll(files);
        // TODO check that length <= MAX_PARAMS
        return result;
    }

    protected abstract String getExecutable();

    protected void addFiles(String... myFiles) {
        addFiles(Arrays.asList(myFiles));
    }

    /**
     * 
     */
    public void terminate() {
        if (consumer != null) {
            consumer.interrupt();
        }
        process.destroy();
    }

}
