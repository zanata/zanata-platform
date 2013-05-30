package org.zanata.transformer;

import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TextFlowTarget;
import com.google.common.base.Objects;

import static com.google.common.base.Objects.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TargetTransformer implements Transformer<TextFlowTarget, HTextFlowTarget>
{
   @Override
   public boolean transform(TextFlowTarget from, HTextFlowTarget to)
   {
      boolean changed = false;
      if (!equal(from.getContents(), to.getContents()))
      {
         to.setContents(from.getContents());
         changed = true;
      }
      if (!equal(from.getState(), to.getState()))
      {
         to.setState(from.getState());
         changed = true;
      }
      return changed;
   }
}
