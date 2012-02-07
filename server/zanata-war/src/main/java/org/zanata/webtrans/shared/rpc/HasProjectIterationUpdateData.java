package org.zanata.webtrans.shared.rpc;

import org.zanata.common.EntityStatus;

public interface HasProjectIterationUpdateData extends HasProjectUpdateData
{
   String getProjectIterationSlug();

   EntityStatus getProjectIterationStatus();
}
