package org.fedorahosted.flies.webtrans.client.ui;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class HeadingPanel extends HorizontalPanel {

	private Widget mainWidget;
	private Button collapseButton;

	public HeadingPanel() {
		setStyleName("gwt-HeadingPanel");
	}
	
	public HeadingPanel(Widget widget) {
		this();
	}
	
	public void setHeadingWidget(Widget widget) {
		
		//add(collapseButton);
		//getCellElement(0, 1).appendChild(heading.getElement());
		//adopt(this.heading);
	}
/*
	public Iterator<Widget> iterator() {
		final Iterator<Widget> superIterator = super.iterator();
		return new Iterator<Widget>() {
			boolean hasTitle = heading != null;

			public boolean hasNext() {
				return superIterator.hasNext() || hasTitle;
			}

			public Widget next() {
				if (superIterator.hasNext()) {
					return superIterator.next();
				} else if (hasTitle) {
					hasTitle = false;
					return heading;
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	*/
	
	public Widget getWidget() {
		return this;
	}
}
