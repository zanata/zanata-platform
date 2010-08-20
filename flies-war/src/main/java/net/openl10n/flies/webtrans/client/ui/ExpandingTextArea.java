package net.openl10n.flies.webtrans.client.ui;

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

public class ExpandingTextArea extends TextArea
{

   public ExpandingTextArea()
   {
      super();
      initialize();
   }

   public ExpandingTextArea(Element element)
   {
      super(element);
      initialize();
   }

   int expandMin = 0;
   int expandMax = 9999;
   
   private void initialize()
   {
      getElement().getStyle().setPaddingTop(0, Unit.PX);
      getElement().getStyle().setPaddingBottom(0, Unit.PX);
      addFocusHandler(new FocusHandler()
      {
         @Override
         public void onFocus(FocusEvent event)
         {
            resizeToContents();
         }
      });
      
      addKeyUpHandler(new KeyUpHandler()
      {
         @Override
         public void onKeyUp(KeyUpEvent event)
         {
            resizeToContents();
         }
      });
   }
   
   int valLength;
   int boxWidth;
   
   private void resizeToContents()
   {
      
      int vlen = getText().length();
      int ewidth = getElement().getOffsetWidth();

      Element e = getElement();
      
      boolean hCheck = true;
      
      if(vlen != valLength || ewidth != boxWidth)
      {
         if(hCheck &&  (vlen < valLength || ewidth != boxWidth))
         {
            e.getStyle().setHeight(0, Unit.PX);
         }
         int h = Math.max(expandMin, Math.min(e.getScrollHeight(), expandMax));
         
         e.getStyle().setOverflow( e.getScrollHeight() > h ? Overflow.AUTO : Overflow.HIDDEN);
         e.getStyle().setHeight(h, Unit.PX);
         
         valLength = vlen;
         boxWidth = ewidth;
      }
      
      
//      var hCheck = !($.browser.msie || $.browser.opera);
//
//      // resize a textarea
//      function ResizeTextarea(e) {
//
//         // event or initialize element?
//         e = e.target || e;
//
//         // find content length and box width
//         var vlen = e.value.length, ewidth = e.offsetWidth;
//         if (vlen != e.valLength || ewidth != e.boxWidth) {
//
//            if (hCheck && (vlen < e.valLength || ewidth != e.boxWidth)) e.style.height = "0px";
//            var h = Math.max(e.expandMin, Math.min(e.scrollHeight, e.expandMax));
//
//            e.style.overflow = (e.scrollHeight > h ? "auto" : "hidden");
//            e.style.height = h + "px";
//
//            e.valLength = vlen;
//            e.boxWidth = ewidth;
//         }
//
//         return true;
//      };
   }
}
