package org.zanata.webtrans.client.ui;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

@ImplementedBy(TranslationWidget.class)
public interface TranslationDisplay extends IsWidget
{
   SafeHtml toSafeHtml();

}
