package org.zanata.concordion;

import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
/**
 * This concordion extension will inject and link css and js resources into spec html
 */
public class CustomResourceExtension implements ConcordionExtension
{

   public static final String JQUERY_PATH = "/org/zanata/feature/jquery-1.8.3.min.js";
   public static final String CSS_PATH = "/org/zanata/feature/spec.css";
   public static final String JS_PATH = "/org/zanata/feature/spec.js";

   @Override
   public void addTo(ConcordionExtender concordionExtender)
   {
      concordionExtender.withLinkedCSS(CSS_PATH, new Resource(CSS_PATH));
      concordionExtender.withLinkedJavaScript(JQUERY_PATH, new Resource(JQUERY_PATH));
      concordionExtender.withLinkedJavaScript(JS_PATH, new Resource(JS_PATH));
   }
}
