package com.weborient.codemirror.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author samangiahi
 *
 */
public class CodeMirrorJSNI {
	private JavaScriptObject editorObject;
	private String jsDirectory = GWT.getModuleBaseURL() + "js/";
	public CodeMirrorJSNI() {
		this(new CodeMirrorConfiguration());
	}

	public CodeMirrorJSNI(CodeMirrorConfiguration configuration) {
		editorObject = initCodeMirror(configuration);
	}

	public native JavaScriptObject initCodeMirror(CodeMirrorConfiguration conf) /*-{
	            
	            var id = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::getId()();
	            var h = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::getHeight()();
	            var ro = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::isReadOnly()();
	            var cs = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::getContinuousScanning()();
	            var ln = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::isLineNumbers()();
	            var tr = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::isTextWrapping()();
	            var su = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::getStyleUrl()();

	            var editor = $wnd.CodeMirror.fromTextArea(id, {
	                height:             h,
	                parserfile: "parsexml.js",
	                stylesheet: su,
	                path: this.@com.weborient.codemirror.client.CodeMirrorJSNI::jsDirectory,
	                continuousScanning: cs,
	                lineNumbers: ln,
	                readOnly: ro,
	                textWrapping: tr,
	                tabMode: "spaces",
	                content: ' '
	              });
	              
	              return editor;
	    }-*/;

	public native String getEditorCode()/*-{
	             var ed = this.@com.weborient.codemirror.client.CodeMirrorJSNI::editorObject; 
	             return ed.getCode();
	     }-*/;

	public native void setEditorCode(String code)/*-{
	             var txed = this.@com.weborient.codemirror.client.CodeMirrorJSNI::editorObject;
	             $wnd.setTimeout(function(a, b){
	                     txed.setCode(code);
	             }, 1000);       
	     }-*/;

	public native void undoEditor()/*-{
			if (this.@com.weborient.codemirror.client.CodeMirrorJSNI::getEditorCode()() != ' ') {
		         var ed = this.@com.weborient.codemirror.client.CodeMirrorJSNI::editorObject;
		         ed.undo();
			}
	}-*/;

	public native void redoEditor()/*-{
			if (this.@com.weborient.codemirror.client.CodeMirrorJSNI::getEditorCode()() != ' ') {
	             var ed = this.@com.weborient.codemirror.client.CodeMirrorJSNI::editorObject;
	             ed.redo();
			}
     }-*/;

	public native void reindentEditor()/*-{
			if (this.@com.weborient.codemirror.client.CodeMirrorJSNI::getEditorCode()() != ' ') {
	             var ed = this.@com.weborient.codemirror.client.CodeMirrorJSNI::editorObject;
	             ed.reindent();
			}
	     }-*/;

	public native void replaceText(String text)/*-{
	             var ed = this.@com.weborient.codemirror.client.CodeMirrorJSNI::editorObject;
	             ed.replaceSelection(text);
	     }-*/;

	public JavaScriptObject getEditorObject() {
		return editorObject;
	}

	public void setEditorObject(JavaScriptObject editorObject) {
		this.editorObject = editorObject;
	}
}
