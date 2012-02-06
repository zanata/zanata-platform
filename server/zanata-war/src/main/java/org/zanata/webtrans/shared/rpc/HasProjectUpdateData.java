package org.zanata.webtrans.shared.rpc;

import org.zanata.common.EntityStatus;


public interface HasProjectUpdateData
{
   String getProjectSlug();

   EntityStatus getProjectStatus();
}
