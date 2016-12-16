package org.zanata.webtrans.shared.validation.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.shared.model.ValidationId;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PrintfXSIExtensionValidation extends PrintfVariablesValidation {
    // regex to find out whether the variable has position
    private static final RegExp POSITIONAL_REG_EXP = RegExp
            .compile("%(\\d+\\$)\\w+");

    public PrintfXSIExtensionValidation(ValidationId id,
            ValidationMessages messages) {
        super(id, messages.printfXSIExtensionValidationDesc(), messages);
    }

    @Override
    public List<String> doValidate(String source, String target) {
        ArrayList<String> errors = new ArrayList<String>();

        ArrayList<String> sourceVars = findVars(source);
        ArrayList<String> targetVars = findVars(target);

        if (PrintfXSIExtensionValidation.hasPosition(targetVars)) {
            sourceVars =
                    PrintfXSIExtensionValidation.appendPosition(sourceVars);
            errors.addAll(checkPosition(targetVars, sourceVars.size()));
        }

        String message = findMissingVariables(sourceVars, targetVars);
        if (!Strings.isNullOrEmpty(message)) {
            errors.add(message);
        }
        message = findAddedVariables(sourceVars, targetVars);
        if (!Strings.isNullOrEmpty(message)) {
            errors.add(message);
        }
        return errors;
    }

    private static boolean hasPosition(ArrayList<String> variables) {
        for (String testVar : variables) {
            MatchResult result = POSITIONAL_REG_EXP.exec(testVar);
            if (result != null) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<String>
            appendPosition(ArrayList<String> sourceVars) {
        ArrayList<String> result = Lists.newArrayList();
        for (int i = 0; i < sourceVars.size(); i++) {
            String sourceVar = sourceVars.get(i);
            int position = i + 1;
            result.add(sourceVar.replace("%", "%" + position + "$"));
        }
        return result;
    }

    private List<String> checkPosition(ArrayList<String> variables, int size) {
        ArrayList<String> errors = new ArrayList<String>();

        Multimap<Integer, String> posToVars = ArrayListMultimap.create();
        for (String testVar : variables) {
            MatchResult result =
                    PrintfXSIExtensionValidation.POSITIONAL_REG_EXP
                            .exec(testVar);
            if (result != null) {
                String positionAndDollar = result.getGroup(1);
                int position =
                        PrintfXSIExtensionValidation
                                .extractPositionIndex(positionAndDollar);
                if (position >= 0 && position < size) {
                    posToVars.put(position, testVar);
                } else {
                    errors.add(getMessages().varPositionOutOfRange(testVar));
                }
            } else {
                errors.add(getMessages().mixVarFormats());
            }
        }
        if (posToVars.keySet().size() != variables.size()) {
            // has some duplicate positions
            for (Map.Entry<Integer, Collection<String>> entry : posToVars
                    .asMap().entrySet()) {
                if (entry.getValue().size() > 1) {
                    errors.add(getMessages().varPositionDuplicated(
                            entry.getValue()));
                }
            }
        }

        return errors;
    }

    private static int extractPositionIndex(String positionAndDollar) {
        try {
            return Integer.parseInt(positionAndDollar.substring(0,
                    positionAndDollar.length() - 1)) - 1;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public String getSourceExample() {
        return "value must be between %x$1 and %y$2";
    }

    @Override
    public String getTargetExample() {
        return "value must be between %x$1 and <span class='js-example__target txt--warning'>%y$3</span>";
    }
}
