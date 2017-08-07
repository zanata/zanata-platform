package org.zanata.transformer;

import java.util.Set;

import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.TextFlowTargetExtension;
import org.zanata.rest.dto.resource.ExtensionSet;
import org.zanata.rest.dto.resource.TextFlowTarget;

import static com.google.common.base.Objects.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TargetTransformer implements
        Transformer<TextFlowTarget, HTextFlowTarget> {
    private final TargetCommentTransformer targetCommentTransformer =
            new TargetCommentTransformer();
    private final Set<String> enabledExtension;

    public TargetTransformer(Set<String> enabledExtension) {
        this.enabledExtension = enabledExtension;
    }

    @Override
    public boolean transform(TextFlowTarget from, HTextFlowTarget to) {
        boolean changed = false;
        if (!equal(from.getContents(), to.getContents())) {
            to.setContents(from.getContents());
            changed = true;
        }
        if (!equal(from.getState(), to.getState())) {
            to.setState(from.getState());
            changed = true;
        }
        changed |=
                transferFromTextFlowTargetExtensions(from.getExtensions(true),
                        to);
        return changed;
    }

    private boolean transferFromTextFlowTargetExtensions(
            ExtensionSet<TextFlowTargetExtension> extensions,
            HTextFlowTarget hTarget) {
        boolean changed = false;
        if (enabledExtension.contains(SimpleComment.ID)) {
            SimpleComment comment = extensions.findByType(SimpleComment.class);
            if (comment != null) {
                changed = targetCommentTransformer.transform(comment, hTarget);
            }
        }

        return changed;

    }
}
