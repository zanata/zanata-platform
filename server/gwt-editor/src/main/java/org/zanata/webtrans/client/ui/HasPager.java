package org.zanata.webtrans.client.ui;

import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.HasValue;

public interface HasPager extends HasPageCount, HasValue<Integer>,
        HasValueChangeHandlers<Integer>, HasFocusHandlers, HasBlurHandlers {

}
