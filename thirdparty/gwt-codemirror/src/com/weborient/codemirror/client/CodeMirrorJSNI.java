package com.weborient.codemirror.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author samangiahi
 *
 */
public class CodeMirrorJSNI implements ICodeMirrorJSNI {
	private JavaScriptObject editorObject;
	@SuppressWarnings("unused") // used by js in initCodeMirror()
	private String jsDirectory = GWT.getModuleBaseURL() + "js/";
	private String parserName;
	public CodeMirrorJSNI() {
		this(new CodeMirrorConfiguration(), " ", SyntaxLanguage.MIXED);
	}

	public CodeMirrorJSNI(CodeMirrorConfiguration configuration) {
		this(configuration, " ", SyntaxLanguage.MIXED);
	}

	public CodeMirrorJSNI(String initialText) {
		this(new CodeMirrorConfiguration(), initialText, SyntaxLanguage.MIXED);
	}

	public CodeMirrorJSNI(CodeMirrorConfiguration configuration, String initialText, SyntaxLanguage syntax) {
		this.parserName = syntax.getParserName();
		this.editorObject = initCodeMirror(configuration, initialText);
	}

	public native JavaScriptObject initCodeMirror(CodeMirrorConfiguration conf, String initialText) /*-{
		var log = function (msg) { 
			@com.google.gwt.core.client.GWT::log(Ljava/lang/String;Ljava/lang/Throwable;) 
				(msg, null)
		};
		log("initCodeMirror");
				var parserName = this.@com.weborient.codemirror.client.CodeMirrorJSNI::parserName;
//				$wnd.Editor.Parser = eval("$wnd."+parserName);
	            var id = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::getId()();
	            var h = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::getHeight()();
	            var ro = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::isReadOnly()();
	            var cs = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::getContinuousScanning()();
	            var ln = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::isLineNumbers()();
	            var tr = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::isTextWrapping()();
//	            var su = conf.@com.weborient.codemirror.client.CodeMirrorConfiguration::getStyleUrl()();

	            var editor = $wnd.CodeMirror.fromTextArea(id, {
	                height:             h,
//	                parserfile: "parsexml.js",
//	                stylesheet: su,
	                parserfile: ["parsexml.js", "parsecss.js", 
	                	"tokenizejavascript.js", "parsejavascript.js", 
	                	"parsehtmlmixed.js", "parsedummy.js"],
 					stylesheet: ["css/xmlcolors.css", "css/jscolors.css", "css/csscolors.css"],
	                path: this.@com.weborient.codemirror.client.CodeMirrorJSNI::jsDirectory,
	                continuousScanning: cs,
	                lineNumbers: ln,
	                readOnly: ro,
	                textWrapping: tr,
	                tabMode: "spaces",
	                content: initialText
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
			// FIXME this condition needs refining
			if (this.@com.weborient.codemirror.client.CodeMirrorJSNI::getEditorCode()() != ' ') {
		         var ed = this.@com.weborient.codemirror.client.CodeMirrorJSNI::editorObject;
		         ed.undo();
			}
	}-*/;

	public native void redoEditor()/*-{
			// FIXME this condition needs refining
			if (this.@com.weborient.codemirror.client.CodeMirrorJSNI::getEditorCode()() != ' ') {
	             var ed = this.@com.weborient.codemirror.client.CodeMirrorJSNI::editorObject;
	             ed.redo();
			}
     }-*/;

	public native void reindentEditor()/*-{
			// FIXME this condition needs refining
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
	
	public SyntaxLanguage getSyntax() {
		return SyntaxLanguage.valueOf(parserName);
	}

	public void setEditorObject(JavaScriptObject editorObject) {
		this.editorObject = editorObject;
	}
	
	public void setSyntax(SyntaxLanguage syntax) {
		this.parserName = syntax.getParserName();
		setParser(parserName);
	}

	private native void setParser(String parserName)/*-{
		var log = function (msg) { 
			@com.google.gwt.core.client.GWT::log(Ljava/lang/String;Ljava/lang/Throwable;) 
				(msg, null)
		};
		log("setParser");
  		var ed = this.@com.weborient.codemirror.client.CodeMirrorJSNI::editorObject;
		log("setParser: ed="+ed);
  		ed.setParser(parserName);
	}-*/;

}
