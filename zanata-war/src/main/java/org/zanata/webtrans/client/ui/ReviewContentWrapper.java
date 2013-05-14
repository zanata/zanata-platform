package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.view.NeedsRefresh;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.ImplementedBy;

@ImplementedBy(ReviewContentWidget.class)
public interface ReviewContentWrapper extends HasUpdateValidationWarning, HasText, NeedsRefresh
{
}
