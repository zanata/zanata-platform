package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.shared.model.DiffMode;
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

   private TextContentsDisplay(List<String> originalContents, List<String> diffContent, DiffMode diffMode)
   {
      if (diffMode == DiffMode.HIGHLIGHT)
      {
         safeHtml = convertToSafeHtmlAsDiffHighlight(originalContents, diffContent);
      }
      else
      {
         safeHtml = convertToSafeHtmlAsDiff(originalContents, diffContent);
      }
   }

   public static TextContentsDisplay asSyntaxHighlightAndSearch(Iterable<String> contents, String highlightString)
   {
      return new TextContentsDisplay(contents, highlightString);
   }

   public static TextContentsDisplay asSyntaxHighlight(Iterable<String> contents)
   {
      return new TextContentsDisplay(contents, EMPTY_SEARCH_TERM);
   }

   public static TextContentsDisplay asDiff(List<String> originalContents, List<String> diffContents)
   {
      return new TextContentsDisplay(originalContents, diffContents, DiffMode.NORMAL);
   }

   public static TextContentsDisplay asDiffHighlight(List<String> originalContents, List<String> diffContents)
   {
      return new TextContentsDisplay(originalContents, diffContents, DiffMode.HIGHLIGHT);
   }

   private static SafeHtml convertToSafeHtmlAsDiff(List<String> originalContents, List<String> diffContents)
   {
      SafeHtmlBuilder builder = new SafeHtmlBuilder();
      for (int i = 0; i < originalContents.size(); i++)
      {
         DiffMatchPatchLabel label = DiffMatchPatchLabel.normalDiff();
         label.setOriginal(originalContents.get(i));
         label.setText(diffContents.get(i));
         appendContent(builder, label.getElement().getString());
      }
      return builder.toSafeHtml();
   }

   private static SafeHtml convertToSafeHtmlAsDiffHighlight(List<String> originalContents, List<String> diffContents)
   {
      SafeHtmlBuilder builder = new SafeHtmlBuilder();
      for (int i = 0; i < originalContents.size(); i++)
      {
         DiffMatchPatchLabel label = DiffMatchPatchLabel.highlightDiff();
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
            label.highlight(highlightString);
         }
         appendContent(builder, label.getElement().getString());
      }
      return builder.toSafeHtml();
   }

   private static void appendContent(SafeHtmlBuilder sb, String content)
   {
      sb.appendHtmlConstant("<div class='textFlowEntry'>").appendHtmlConstant(content).appendHtmlConstant("</div>");
   }

   public SafeHtml toSafeHtml()
   {
      return safeHtml;
   }
}
