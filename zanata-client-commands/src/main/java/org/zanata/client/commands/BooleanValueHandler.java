package org.zanata.client.commands;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * Boolean {@link OptionHandler}.
 *
 * @author Kohsuke Kawaguchi
 */
public class BooleanValueHandler extends OptionHandler<Boolean> {
    private static final List<String> ACCEPTABLE_VALUES = Arrays
            .asList(new String[] { "true", "on", "yes", "1", "false", "off",
                    "no", "0" });

    public BooleanValueHandler(CmdLineParser parser, OptionDef option,
            Setter<? super Boolean> setter) {
        super(parser, option, setter);
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        String valueStr = params.getParameter(0).toLowerCase();
        int index = ACCEPTABLE_VALUES.indexOf(valueStr);
        if (index == -1) {
            throw new CmdLineException(MessageFormat.format(
                    "\"{0}\" is not a legal boolean value", valueStr));
        }
        setter.addValue(index < ACCEPTABLE_VALUES.size() / 2);
        return 1;
    }

    @Override
    public String getDefaultMetaVariable() {
        return "BOOL";
    }
}
