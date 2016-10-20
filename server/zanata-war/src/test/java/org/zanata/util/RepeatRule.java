package org.zanata.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

// https://gist.github.com/fappel/8bcb2aea4b39ff9cfb6e
public class RepeatRule implements TestRule {

    private static class RepeatStatement extends Statement {
        private final int times;
        private final Statement statement;

        private RepeatStatement(int times, Statement statement) {
            this.times = times;
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            for (int i = 0; i < times; i++) {
                statement.evaluate();
            }
        }
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        Statement result = statement;
        Repeat repeat = description.getAnnotation(Repeat.class);
        if (repeat != null) {
            result = new RepeatStatement(repeat.times(), statement);
        }
        return result;
    }
}
