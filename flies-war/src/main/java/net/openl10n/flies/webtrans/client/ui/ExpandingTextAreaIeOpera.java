package net.openl10n.flies.webtrans.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;

public class ExpandingTextAreaIeOpera extends ExpandingTextArea
{
   /**
    * Creates an empty text area.
    */
   public ExpandingTextAreaIeOpera()
   {
      super();
   }

   /**
    * This constructor may be used by subclasses to explicitly use an existing
    * element. This element must be a &lt;textarea&gt; element.
    * 
    * @param element the element to be used
    */
   public ExpandingTextAreaIeOpera(Element element)
   {
      super(element);
   }

   /**
    * Creates an empty text area.
    * 
    * @param expandMin minimum visible rows of text
    * @Param expandMax maximum rows of text to expand the area to
    */
   public ExpandingTextAreaIeOpera(int expandMin, int expandMax)
   {
      super(expandMin, expandMax);
   }

   /**
    * This constructor may be used by subclasses to explicitly use an existing
    * element. This element must be a &lt;textarea&gt; element.
    * 
    * @param element the element to be used
    * @param expandMin minimum visible rows of text
    * @Param expandMax maximum rows of text to expand the area to
    */
   public ExpandingTextAreaIeOpera(Element element, int expandMin, int expandMax)
   {
      super(element, expandMin, expandMax);
   }

   @Override
   protected void resizeToContents()
   {

      int vlen = getText().length();
      int ewidth = getElement().getOffsetWidth();

      Element e = getElement();

      if (vlen != valLength || ewidth != boxWidth)
      {
         int h = Math.max(expandMin, Math.min(e.getScrollHeight(), expandMax));

         e.getStyle().setOverflow(e.getScrollHeight() > h ? Overflow.AUTO : Overflow.HIDDEN);
         e.getStyle().setHeight(h, Unit.PX);

         valLength = vlen;
         boxWidth = ewidth;
      }
   }
   
}
