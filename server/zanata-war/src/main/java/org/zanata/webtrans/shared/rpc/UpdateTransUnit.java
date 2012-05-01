package org.zanata.webtrans.shared.rpc;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnitId;

import java.util.ArrayList;
import java.util.List;


public class UpdateTransUnit extends AbstractWorkspaceAction<UpdateTransUnitResult>
{
   private static final long serialVersionUID = 1L;

   private List<TransUnitId> transUnitId;
   private List<ArrayList<String>> contents;
   private List<ContentState> contentState;
   private List<Integer> verNum;

   private boolean isRedo = false;

   public boolean isRedo()
   {
      return isRedo;
   }

   public void setRedo(boolean isRedo)
   {
      this.isRedo = isRedo;
   }

   protected UpdateTransUnit()
   {
      transUnitId = new ArrayList<TransUnitId>();
      contents = new ArrayList<ArrayList<String>>();
      contentState = new ArrayList<ContentState>();
      verNum = new ArrayList<Integer>();
   }

   public UpdateTransUnit(TransUnitId transUnitId, ArrayList<String> contents, ContentState contentState, int verNum)
   {
      this();
      addTransUnit(transUnitId, contents, contentState, verNum);
   }

   public void addTransUnit(TransUnitId transUnitId, ArrayList<String> contents, ContentState contentState, int verNum)
   {
      this.transUnitId.add(transUnitId);
      this.contents.add(contents);
      this.contentState.add(contentState);
      this.verNum.add(verNum);
   }

   public List<TransUnitId> getTransUnitIds()
   {
      return transUnitId;
   }

   public TransUnitId getSingleTransUnitId()
   {
      if (transUnitId.size() == 1)
      {
         return transUnitId.get(0);
      }
      else
      {
         throw new IllegalStateException("this method can only be used when updating a single TransUnit");
      }
   }

   public List<ArrayList<String>> getAllContents()
   {
      return contents;
   }

   // FIXME replace with List<String>, requires updating TransUnit and builder.
   public ArrayList<String> getSingleContents()
   {
      if (contents.size() == 1)
      {
      return contents.get(0);
      }
      else
      {
         throw new IllegalStateException("this method can only be used when updating a single TransUnit");
      }
   }

   public List<ContentState> getContentStates()
   {
      return contentState;
   }

   public ContentState getSingleContentState()
   {
      if (contentState.size() == 1)
      {
         return contentState.get(0);
      }
      else
      {
         throw new IllegalStateException("this method can only be used when updating a single TransUnit");
      }
   }

   public List<Integer> getVerNum()
   {
      return verNum;
   }

   public Integer getSingleVerNum()
   {
      if (verNum.size() == 1)
      {
         return verNum.get(0);
      }
      else
      {
         throw new IllegalStateException("this method can only be used when updating a single TransUnit");
      }
   }
}
