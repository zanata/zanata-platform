package org.fedorahosted.flies.webtrans.client.mvp;

import java.util.ArrayList;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.gen2.complexpanel.client.CollapsiblePanel;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.FastTree;
import com.google.gwt.widgetideas.client.FastTreeItem;

public class WestNavigationView extends Composite implements
		WestNavigationPresenter.Display {

	ToggleButton controlButton;
	final Panel contents;
	final CollapsiblePanel panel;
	
	public WestNavigationView() {
		Log.info("setting up LeftNavigationView");

		// Some random contents to make the tree interesting.
		contents = createSchoolNavBar();
		FastTree.addDefaultCSS();

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
		panel.setWidth("220px");

		Label lbl = new Label("x");
		lbl.setWidth("50px");
		panel.setHoverBarContents(lbl);
		panel.hookupControlToggle(controlButton);

	}
	
	private Panel createSchoolNavBar() {
		ToggleButton toggler = new ToggleButton("Directory (click to pin)",
				"Directory (click to collapse)");
		toggler.setStyleName("CollapsibleToggle");
		controlButton = toggler;

		MyStackPanel wrapper = new MyStackPanel();
		FlowPanel navBar = new FlowPanel();
		navBar.setStylePrimaryName("LeftContentNavBar");
		navBar.setSize("100%", "100%");

		HorizontalPanel panel = new HorizontalPanel();
		panel.setWidth("100%");

		panel.setCellHorizontalAlignment(controlButton,
				HasHorizontalAlignment.ALIGN_LEFT);

		panel.add(controlButton);
		panel.setCellWidth(controlButton, "1px");
		panel.setCellHorizontalAlignment(controlButton,
				HorizontalPanel.ALIGN_CENTER);

		navBar.add(panel);

		panel.setStyleName("nav-Tree-title");
		wrapper = new MyStackPanel();
		wrapper.setHeight("250px");

		final FastTree contents = new FastTree();
		wrapper.add(contents, "<b>People</b>", true);

		wrapper.add(new Label("None"), "<b>Academics</b>", true);
		navBar.add(wrapper);

		FastTreeItem students = contents.addItem("Students");
		students.addItem("Jill");
		students.addItem("Jack");
		students.addItem("Molly");
		students.addItem("Ms. Muffat");

		FastTreeItem teachers = contents.addItem("Teachers");
		teachers.addItem("Mrs Black");
		teachers.addItem("Mr White");

		FastTreeItem admin = contents.addItem("Administrators");
		admin.addItem("The Soup Nazi");
		admin.addItem("The Grand High Supreme Master Pubba");
		navBar.add(new Label("heelo"));
		return navBar;
	}

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
		return contents;
	}
	
}
