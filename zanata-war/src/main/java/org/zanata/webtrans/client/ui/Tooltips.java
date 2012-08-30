package org.zanata.webtrans.client.ui;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class Tooltips
{

   public static SafeHtml textWithTooltip(String text, String tooltip)
   {
      SafeHtmlBuilder mysb = new SafeHtmlBuilder();
      mysb.append(SafeHtmlUtils.fromTrustedString("<span title='"+SafeHtmlUtils.htmlEscape(tooltip)+"'>"));
      mysb.appendEscaped(text);
      mysb.appendHtmlConstant("</span>");
      return mysb.toSafeHtml();
   }

}
