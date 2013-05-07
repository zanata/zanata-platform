package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitProvidesKey;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class ReviewTableSelectionModel extends MultiSelectionModel<TransUnit>
{
   public ReviewTableSelectionModel()
   {
      super(TransUnitProvidesKey.KEY_PROVIDER);
   }
}
