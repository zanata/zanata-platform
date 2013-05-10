package org.zanata.webtrans.shared.util;

import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.HasTransUnitId;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class Finds
{
   private Finds()
   {
   }

   public static <D extends HasTransUnitId> Optional<D> findDisplayById(Iterable<D> displayList, TransUnitId currentTransUnitId)
   {
      return Iterables.tryFind(displayList, new FindByTransUnitIdPredicate(currentTransUnitId));
   }
}
