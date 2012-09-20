package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.inject.ImplementedBy;

@ImplementedBy(GoToRowLinkLabel.class)
public interface GoToRowLink extends InlineLink
{
   void prepare(String linkText, TransUnitId transUnitId);
}
