package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.webtrans.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.ProgressBar;

public class TransUnitInfoView extends CaptionPanel implements TransUnitInfoPresenter.Display {

	public TransUnitInfoView() {
		setTitle("Translation Unit Info");
		addBody(getStatusPanel());
	}

	public static Widget getStatusPanel() {
		VerticalPanel vpanel = new VerticalPanel();
		vpanel.setWidth("100%");
		vpanel.setHeight("100px");
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		vpanel.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
				
		//ProgressBar bar = new ProgressBar(0.0, 2000.0,0.0);
		//bar.setProgress(1500.0);
		//bar.setTitle("Complete");
		vpanel.add(new Label("Translation Unit Info"));
		//vpanel.add(bar);
		return vpanel;
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
}
