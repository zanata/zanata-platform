package org.zanata.webtrans.client.view;

import java.util.List;

import org.zanata.webtrans.client.ui.HasUpdateValidationWarning;
import org.zanata.webtrans.client.ui.ReviewContentWrapper;
import org.zanata.webtrans.shared.model.HasTransUnitId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.ImplementedBy;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

@ImplementedBy(ReviewContentsView.class)
public interface ReviewContentsDisplay extends WidgetDisplay, NeedsRefresh, HasTransUnitId, HasUpdateValidationWarning
{
   void setValueAndCreateNewEditors(TransUnit transUnit);

   void setListener(Listener listener);

   List<ReviewContentWrapper> getEditors();

   interface Listener
   {

      void acceptTranslation(TransUnitId id);

      void rejectTranslation(TransUnitId id);
   }
}
