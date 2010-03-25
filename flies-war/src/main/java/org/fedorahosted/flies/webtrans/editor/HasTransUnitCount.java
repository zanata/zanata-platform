package org.fedorahosted.flies.webtrans.editor;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.TransUnitCount;

public interface HasTransUnitCount {

	public static enum CountUnit {
		Word, TranslationUnit;
	}

	public static enum LabelFormat {
		Percentage, Unit;
	}

	public void setCount(int approved, int needReview, int untranslated);
	public void setCount(ContentState state, int count);
	public void setCount(TransUnitCount count);

	public int getCount(ContentState state);
	public int getTotal();
	
	public void setLabelFormat(LabelFormat format);
	public LabelFormat getLabelFormat();
	
	public void setCountUnit(CountUnit countUnit);
	public CountUnit getCountUnit();

	void setToggleEnabled(boolean toggleEnabled);
	boolean isToggleEnabled();

	void setLabelVisible(boolean labelVisible);

	boolean isLabelVisible();
	
	
}
