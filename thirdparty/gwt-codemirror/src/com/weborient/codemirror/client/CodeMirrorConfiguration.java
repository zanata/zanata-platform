package com.weborient.codemirror.client;

import com.google.gwt.core.client.GWT;


/**
 * @author samangiahi
 *
 */
public class CodeMirrorConfiguration {
	
	public String id = String.valueOf(this.hashCode());
	private String height = "350px";
	private String width = "100%";
	private boolean readOnly = false;
	private int continuousScanning = 1000;
	private boolean lineNumbers = true;
	private boolean textWrapping = false;
	private String styleUrl = GWT.getModuleBaseURL() + "css/xmlcolors.css";
	private String[] listBoxPreSets;
	private String tagSelectorLabel;

	/**
	 * @return String 
	 * This method returns the current height of the editor if is set. The default is 350px
	 */
	public String getHeight() {
		return height;
	}

	/**
	 * @param height
	 * This method sets the height of the editor
	 */
	public void setHeight(String height) {
		this.height = height;
	}

	/**
	 * @return boolean 
	 * This method returns the current readonly state of the editor. default is false
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
	
	/**
	 * @param readOnly
	 * This method sets disables the editor and make it for read only perposes
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	/**
	 * @return int 
	 * the time that the editor checks for the changes. the default is 1000
	 */
	public int getContinuousScanning() {
		return continuousScanning;
	}

	/**
	 * @param continuousScanning
	 * sets the time the editor checks for the changes
	 */
	public void setContinuousScanning(int continuousScanning) {
		this.continuousScanning = continuousScanning;
	}
	
	/**
	 * @return boolean 
	 * The current state of the line number.
	 */
	public boolean isLineNumbers() {
		return lineNumbers;
	}

	
	/**
	 * @param lineNumbers
	 * Set the current state of the line number. 
	 * true for visible and false for invisible
	 */ 
	public void setLineNumbers(boolean lineNumbers) {
		this.lineNumbers = lineNumbers;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public boolean isTextWrapping() {
		return textWrapping;
	}

	public void setTextWrapping(boolean textWrapping) {
		this.textWrapping = textWrapping;
	}

	public String getStyleUrl() {
		return styleUrl;
	}

	public void setStyleUrl(String styleUrl) {
		this.styleUrl = styleUrl;
	}

	public String[] getListBoxPreSets() {
		return listBoxPreSets;
	}

	public void setListBoxPreInsert(String[] listBoxPreInsert, String... o) {
		this.listBoxPreSets = listBoxPreInsert;
	}

	public void setListBoxPreSets(String... listBoxPreInsert) {
		this.listBoxPreSets = listBoxPreInsert;
	}

	public String getTagSelectorLabel() {
		return tagSelectorLabel;
	}

	public void setTagSelectorLabel(String tagSelectorLabel) {
		this.tagSelectorLabel = tagSelectorLabel;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
