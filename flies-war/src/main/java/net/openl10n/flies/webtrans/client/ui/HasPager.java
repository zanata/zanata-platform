package net.openl10n.flies.webtrans.client.ui;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.HasValue;

public interface HasPager extends HasPageCount, HasValue<Integer>, HasValueChangeHandlers<Integer>
{

}
