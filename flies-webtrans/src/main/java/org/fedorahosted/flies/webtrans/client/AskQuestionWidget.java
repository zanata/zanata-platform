package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.common.MyService;
import org.fedorahosted.flies.gwt.common.MyServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class AskQuestionWidget extends Composite {

	private AbsolutePanel panel = new AbsolutePanel();

	public AskQuestionWidget() {

		Label lbl = new Label("OK, what do you want to know?");
		panel.add(lbl);
		final TextBox box = new TextBox();
		box.setText("What is the meaning of life?");
		panel.add(box);

		Button ok = new Button("Ask");

		ok.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (!box.getText().endsWith("?")) {
					Window.alert("A question has to end with a '?'");
				} else {
					askServer(box.getText());
				}
			}
		});
		
		panel.add(ok);
		initWidget(panel);

	}

	private void askServer(String text) {
		getService().askIt(text, new AsyncCallback<String>() {
			public void onFailure(Throwable t) {
				Window.alert(t.getMessage());
			}

			public void onSuccess(String data) {
				Window.alert(data);
			}

		});
	}

	private MyServiceAsync getService() {
		String endpointURL = "/flies/seam/resource/gwt";

		MyServiceAsync svc = (MyServiceAsync) GWT.create(MyService.class);
		((ServiceDefTarget) svc).setServiceEntryPoint(endpointURL);

		return svc;

	}
}
