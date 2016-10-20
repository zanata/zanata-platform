package org.zanata.client.commands;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Setter;

/**
 * Boolean {@link OptionHandler} which takes an argument.  Overrides the
 * superclass with a better default meta variable.
 */
public class BooleanValueHandler extends ExplicitBooleanOptionHandler {

    public BooleanValueHandler(CmdLineParser parser, OptionDef option,
            Setter<? super Boolean> setter) {
        super(parser, option, setter);
    }

    @Override
    public String getDefaultMetaVariable() {
        return "BOOL";
    }
}
