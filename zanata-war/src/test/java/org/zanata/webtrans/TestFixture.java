package org.zanata.webtrans;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnit;

public class TestFixture
{
   public static TransUnit makeTransUnit(int id)
   {
      return makeTransUnit(id, ContentState.New);
   }

   public static TransUnit makeTransUnit(int id, ContentState contentState)
   {
      return TransUnit.Builder.newTransUnitBuilder().setId(id).setResId("resId" + id).setVerNum(0)
            .setLocaleId("en").addSource("source").addTargets("target").setStatus(contentState).setRowIndex(id).build();
   }
}
