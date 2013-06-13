package org.zanata.transformer;

import org.zanata.model.HSimpleComment;
import org.zanata.model.HasSimpleComment;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import com.google.common.base.Objects;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TargetCommentTransformer implements Transformer<SimpleComment, HasSimpleComment>
{

   @Override
   public boolean transform(SimpleComment from, HasSimpleComment to)
   {
      HSimpleComment hComment = to.getComment();

      if (hComment == null)
      {
         hComment = new HSimpleComment();
      }
      if (!Objects.equal(from.getValue(), hComment.getComment()))
      {
         hComment.setComment(from.getValue());
         to.setComment(hComment);
         return true;
      }
      return false;
   }
}
