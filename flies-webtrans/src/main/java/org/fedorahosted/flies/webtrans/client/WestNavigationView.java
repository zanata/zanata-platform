package org.fedorahosted.flies.webtrans.client;

import java.util.ArrayList;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.gen2.complexpanel.client.CollapsiblePanel;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.FastTree;
import com.google.gwt.widgetideas.client.FastTreeItem;

public class WestNavigationView extends Composite implements
		WestNavigationPresenter.Display {

	/*
	ToggleButton controlButton;
	final Panel contents;
	final CollapsiblePanel panel;
	 */
	public interface Images extends ImageBundle {
		@Resource("org/fedorahosted/flies/webtrans/images/pin.gif")
		AbstractImagePrototype pin();

		@Resource("org/fedorahosted/flies/webtrans/images/unpin.gif")
		AbstractImagePrototype unpin();
		
		@Resource("org/fedorahosted/flies/webtrans/images/unpin.gif")
		AbstractImagePrototype expand();
	}

	private Images images = (Images) GWT.create(Images.class);

	final VerticalPanel panel;
	
	public WestNavigationView() {
		Log.info("setting up LeftNavigationView");

		panel = new VerticalPanel();
		panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		panel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
		panel.setSpacing(5);
		initWidget(panel);
/*		
		// Some random contents to make the tree interesting.
		contents = createNavBar();

		// The panel.
		panel = new CollapsiblePanel();
		String value = Location.getParameter("collapsed");
		if (value != null) {
			value = value.trim();
			if (value.equals("true")) {
				panel.setCollapsedState(true);
			} else if (value.equals("false")) {
				// do nothing, default.
			} else {
				Window.alert("collapsed should not be given " + value
						+ " use true or false instead");
			}
		}
		initWidget(panel);

		panel.add(contents);
*/		
		panel.setWidth("220px");
/*
		VerticalPanel hoverPanel = new VerticalPanel();
		hoverPanel.setStylePrimaryName("LeftContentNavBar");
		//Image expand = images.expand().createImage();
		Label expand = new Label(">");
		hoverPanel.add(expand);
		hoverPanel.setHeight("100%");
		hoverPanel.setCellVerticalAlignment(expand, VerticalPanel.ALIGN_MIDDLE);
		
		panel.setHoverBarContents(hoverPanel);
		panel.setHoverBarWidth("2px");
		panel.hookupControlToggle(controlButton);
*/
	}

/*	
	private Panel createNavBar() {
		controlButton = new ToggleButton(images.pin().createImage(),
				images.unpin().createImage());
		controlButton.setStyleName("CollapsibleToggle");

		VerticalPanel navBar = new VerticalPanel();
		navBar.setStylePrimaryName("LeftContentNavBar");
		navBar.setSize("100%", "100%");

		HorizontalPanel panel = new HorizontalPanel();
		panel.setWidth("100%");

		panel.add(controlButton);
		//panel.setCellWidth(controlButton, "1px");
		panel.setCellHorizontalAlignment(controlButton,
				HorizontalPanel.ALIGN_RIGHT);

		navBar.add(panel);

		panel.setStyleName("nav-Tree-title");
		
		DecoratorPanel translators = new DecoratorPanel();
		translators.add(new Label("Translators"));
		navBar.add(translators);
		navBar.setCellVerticalAlignment(translators, VerticalPanel.ALIGN_TOP);
		//translators.setWidth("80%");
		return navBar;
	}
*/
	@Override
	public void setHeight(String height) {
		panel.setHeight(height);
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void startProcessing() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopProcessing() {
		// TODO Auto-generated method stub

	}
	/**
	 * A special purpose widget to allow scrollable stack panels.
	 */
	public class MyStackPanel extends StackPanel {
		private ArrayList<Widget> scrollers = new ArrayList<Widget>();

		public void insert(Widget w, int before) {
			ScrollPanel p = new ScrollPanel(w);
			p.setWidth("100%");
			scrollers.add(before, p);
			super.insert(p, before);
		}

		public void onLoad() {
			setWidth("100%");
			showStack(getSelectedIndex());
		}

		/**
		 * Shows the widget at the specified child index.
		 * 
		 * @param index
		 *            the index of the child to be shown
		 */
		public void showStack(int index) {
			super.showStack(index);

			if (this.isAttached()) {
				ScrollPanel me = (ScrollPanel) scrollers.get(index);
				me.setHeight("1px");
				Element tr = DOM.getChild(DOM.getFirstChild(getElement()),
						index * 2 + 1);
				int trHeight = DOM.getElementPropertyInt(tr, "offsetHeight");
				me.setHeight(trHeight + "px");
			}
		}
	}

	@Override
	public HasWidgets getWidgets() {
		return panel;
	}

}
