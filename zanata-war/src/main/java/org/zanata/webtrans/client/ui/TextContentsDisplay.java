package org.zanata.webtrans.client.ui;

import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TextContentsDisplay
{
   private static final String EMPTY_SEARCH_TERM = "";
   private SafeHtml safeHtml;

   private TextContentsDisplay(Iterable<String> contents, String highlightString)
   {
      safeHtml = convertToSafeHtml(contents, highlightString);
   }

   private TextContentsDisplay(List<String> originalContents, List<String> diffContent)
   {
      safeHtml = convertToSafeHtmlAsDiff(originalContents, diffContent);
   }

   public static TextContentsDisplay asSyntaxHighlightAndSearch(Iterable<String> contents, String highlightString)
   {
      return new TextContentsDisplay(contents, highlightString);
   }

   public static TextContentsDisplay asSyntaxHighlight(List<String> contents)
   {
      return new TextContentsDisplay(contents, EMPTY_SEARCH_TERM);
   }

   public static TextContentsDisplay asDiff(List<String> originalContents, List<String> diffContents)
   {
      return new TextContentsDisplay(originalContents, diffContents);
   }

   private static SafeHtml convertToSafeHtmlAsDiff(List<String> originalContents, List<String> diffContents)
   {
      SafeHtmlBuilder builder = new SafeHtmlBuilder();
      for (int i = 0; i < originalContents.size(); i++)
      {
         DiffMatchPatchLabel label = new DiffMatchPatchLabel();
         label.setOriginal(originalContents.get(i));
         label.setText(diffContents.get(i));
         appendContent(builder, label.getElement().getString());
      }
      return builder.toSafeHtml();
   }

   private static SafeHtml convertToSafeHtml(Iterable<String> contents, String highlightString)
   {
      SafeHtmlBuilder builder = new SafeHtmlBuilder();
      for (String content : contents)
      {
         HighlightingLabel label = new HighlightingLabel(content);
         if (!Strings.isNullOrEmpty(highlightString))
         {
            label.highlightSearch(highlightString);
         }
         appendContent(builder, label.getElement().getString());
      }
      return builder.toSafeHtml();
   }

   private static void appendContent(SafeHtmlBuilder sb, String content)
   {
      // TODO hardcoded css - move to Application.css
      sb.appendHtmlConstant("<div class='translationContainer' style='border-bottom: dotted 1px grey;'>").appendHtmlConstant(content).appendHtmlConstant("</div>");
   }

   public SafeHtml toSafeHtml()
   {
      if (safeHtml == null)
      {
         Log.warn("you probably forget to call one of the asSyntaxHighlight or asDiff method?!");
         safeHtml = new SafeHtmlBuilder().toSafeHtml();
      }
      return safeHtml;
   }
}
