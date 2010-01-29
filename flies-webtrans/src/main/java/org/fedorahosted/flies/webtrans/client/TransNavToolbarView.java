package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TransNavToolbarView extends HorizontalPanel implements TransNavToolbarPresenter.Display{
	
	private Button
						nextEntryButton, prevEntryButton, 
						nextFuzzyButton, prevFuzzyButton,
						nextUntranslatedButton, prevUntranslatedButton;
	
	public TransNavToolbarView() {
		
		prevEntryButton = new Button("Prev Entry"); 
		nextEntryButton = new Button("Next Entry");
		prevFuzzyButton = new Button("Prev Fuzzy");
		nextFuzzyButton = new Button("next Fuzzy");
		prevUntranslatedButton = new Button("Prev Untranslated");
		nextUntranslatedButton = new Button("Next Untranslated");
		
		add(prevEntryButton);
		add(nextEntryButton);
		add(prevFuzzyButton);
		add(nextFuzzyButton);
		add(prevUntranslatedButton);
		add(nextUntranslatedButton);
	}
	@Override
	public Button getNextEntryButton() {
		// TODO Auto-generated method stub
		return nextEntryButton;
	}
	@Override
	public Button getNextFuzzyButton() {
		// TODO Auto-generated method stub
		return nextFuzzyButton;
	}
	@Override
	public Button getNextUntranslatedButton() {
		// TODO Auto-generated method stub
		return nextUntranslatedButton;
	}
	@Override
	public Button getPrevEntryButton() {
		// TODO Auto-generated method stub
		return prevEntryButton;
	}
	@Override
	public Button getPrevFuzzyButton() {
		// TODO Auto-generated method stub
		return prevFuzzyButton;
	}
	@Override
	public Button getPrevUntranslatedButton() {
		// TODO Auto-generated method stub
		return nextUntranslatedButton;
	}
	@Override
	public Widget asWidget() {
		// TODO Auto-generated method stub
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
