package org.zanata.util;

import java.util.Comparator;

import org.zanata.model.HTextFlow;

/**
* @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
public enum HTextFlowPosComparator implements Comparator<HTextFlow>
{
   INSTANCE;

   @Override
   public int compare(HTextFlow one, HTextFlow other)
   {
      return one.getPos().compareTo(other.getPos());
   }
}
