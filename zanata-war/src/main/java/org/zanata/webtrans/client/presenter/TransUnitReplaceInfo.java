package org.zanata.webtrans.client.presenter;

import java.util.Comparator;

import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdatePreview;

public class TransUnitReplaceInfo
{
   private static Comparator<TransUnitReplaceInfo> comparator;

   private ReplacementState state;
   private Long docId;
   private TransUnit tu;
   private TransUnitUpdatePreview preview;
   private TransUnitUpdateInfo replaceInfo;

   public TransUnitReplaceInfo(Long containingDocId, TransUnit tu)
   {
      this.docId = containingDocId;
      this.tu = tu;
      preview = null;
      replaceInfo = null;
      state = ReplacementState.Replaceable;
   }

   public TransUnit getTransUnit()
   {
      return tu;
   }

   public void setTransUnit(TransUnit tu)
   {
      this.tu = tu;
   }

   public TransUnitUpdatePreview getPreview()
   {
      return preview;
   }

   public void setPreview(TransUnitUpdatePreview preview)
   {
      this.preview = preview;
   }

   public TransUnitUpdateInfo getReplaceInfo()
   {
      return replaceInfo;
   }

   public void setReplaceInfo(TransUnitUpdateInfo replaceInfo)
   {
      this.replaceInfo = replaceInfo;
   }

   public ReplacementState getState()
   {
      return state;
   }

   public void setState(ReplacementState state)
   {
      this.state = state;
   }

   public Long getDocId()
   {
      return docId;
   }


   public static Comparator<TransUnitReplaceInfo> getComparator()
   {
      if (comparator == null)
      {
         comparator = new Comparator<TransUnitReplaceInfo>()
         {

            @Override
            public int compare(TransUnitReplaceInfo o1, TransUnitReplaceInfo o2)
            {
               if (o1 == o2)
               {
                  return 0;
               }
               if (o1 != null)
               {
                  return (o2 != null ? Integer.valueOf(o1.getTransUnit().getRowIndex()).compareTo(o2.getTransUnit().getRowIndex()) : 1);
               }
               return -1;
            }
         };
      }
      return comparator;
   }
}