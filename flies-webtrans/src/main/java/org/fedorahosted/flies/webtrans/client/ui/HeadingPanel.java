package org.fedorahosted.flies.webtrans.client.ui;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeadingPanel extends DecoratorPanel {

	private Widget heading;

	public HeadingPanel() {
		addStyleName("gwt-HeadingPanel");
	}
	
	public void setHeadingWidget(Widget heading) {
		this.heading = heading;
		getCellElement(0, 1).appendChild(heading.getElement());
		adopt(heading);
	}

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
}
