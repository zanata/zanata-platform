package org.zanata.client.commands.pull;

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
}
