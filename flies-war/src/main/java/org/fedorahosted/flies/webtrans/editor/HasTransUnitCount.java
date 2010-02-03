package org.fedorahosted.flies.webtrans.editor;

public interface HasTransUnitCount {
	 public void setStatus(int fuzzy, int translated, int untranslated);
	 public void setFuzzy(int fuzzy); 
	 public void setTranslated(int translated); 
	 public void setUntranslated(int untranslated);
	 public int getFuzzy();
	 public int getTranslated();
	 public int getUntranslated();
}
