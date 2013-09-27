package org.zanata.webtrans.client.ui;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class CodeMirrorReadOnlyWidget extends Composite implements
        SourceContentWrapper {
    private static CodeMirrorWidgetUiBinder ourUiBinder = GWT
            .create(CodeMirrorWidgetUiBinder.class);

    @UiField
    TextAreaElement textArea;

    private JavaScriptObject codeMirror;
    private String content;

    public CodeMirrorReadOnlyWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    // see http://codemirror.net/doc/manual.html#usage
    private native JavaScriptObject initCodeMirror(Element element) /*-{
    var self = this;
    var codeMirrorEditor = $wnd.CodeMirror.fromTextArea(element, {
      lineNumbers : true,
      lineWrapping : true,
      mode : "visibleSpace",
      readOnly : true
    });

    return codeMirrorEditor;

    }-*/;

    @Override
    public String getText() {
        return content;
    }

    @Override
    public void setText(String text) {
        textArea.setValue(text);
        if (codeMirror == null) {
            codeMirror = initCodeMirror(textArea);
        }
        content = text;
    }

    public native void refresh() /*-{
    var codeMirror = this.@org.zanata.webtrans.client.ui.CodeMirrorReadOnlyWidget::codeMirror;
    if (codeMirror) {
      codeMirror.refresh();
    }
    }-*/;

    public void highlight(String term) {
        if (!Strings.isNullOrEmpty(term)) {
            codeMirrorHighlight(term);
        }
    }

    private native void codeMirrorHighlight(String term) /*-{
    var codeMirror = this.@org.zanata.webtrans.client.ui.CodeMirrorReadOnlyWidget::codeMirror;
    var searchCursor = codeMirror.getSearchCursor(term, {
      line : 0,
      ch : 0
    }, true);
    while (searchCursor.findNext()) {
      codeMirror.markText(searchCursor.from(), searchCursor.to(),
          "CodeMirror-searching");
    }
    }-*/;

    interface CodeMirrorWidgetUiBinder extends
            UiBinder<Widget, CodeMirrorReadOnlyWidget> {
    }
}
