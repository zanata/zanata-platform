package org.zanata.webtrans.server.rpc;

import org.zanata.model.HSimpleComment;

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
