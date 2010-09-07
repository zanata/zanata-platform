package net.openl10n.flies.webtrans.server.rpc;

import net.openl10n.flies.model.HSimpleComment;

public class CommentsUtil
{

   public static String toString(HSimpleComment comment)
   {
      if (comment == null)
         return null;
      else
         return comment.getComment();
   }

}
