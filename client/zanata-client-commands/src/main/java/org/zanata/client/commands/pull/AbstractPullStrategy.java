package org.zanata.client.commands.pull;

import org.zanata.client.commands.TransFileResolver;
import org.zanata.client.config.LocaleMapping;

import java.io.File;

import org.zanata.client.commands.DocNameWithoutExt;

public abstract class AbstractPullStrategy implements PullStrategy {
    private final PullOptions opts;

    protected AbstractPullStrategy(PullOptions opts) {
        this.opts = opts;
    }

    public PullOptions getOpts() {
        return opts;
    }

    @Override
    public boolean isTransOnly() {
        return false;
    }

    @Override
    public File getTransFileToWrite(String docName,
        LocaleMapping localeMapping) {
        return new TransFileResolver(getOpts()).getTransFile(
            DocNameWithoutExt.from(docName), localeMapping);
    }
}
