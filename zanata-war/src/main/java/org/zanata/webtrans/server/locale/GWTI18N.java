package org.zanata.webtrans.server.locale;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.i18n.client.Messages;

public class GWTI18N
{
   public final static Map<String, Object> cache = new HashMap<String, Object>();
   public static boolean useCache = true;

   public static <T> T create(Class<T> itf) throws IOException
   {
      return create(itf, null);
   }

   @SuppressWarnings("unchecked")
   public static <T> T create(Class<T> itf, String lang) throws IOException
   {
      final String key = itf.getName() + (lang == null ? "" : ("_" + lang));
      if (useCache)
      {
         Object msg = cache.get(key);
         if (msg == null)
         {
            msg = createProxy(itf, lang);
            cache.put(key, msg);
         }
         return (T) msg;
      }
      else
      {
         return createProxy(itf, lang);
      }
   }

   @SuppressWarnings("unchecked")
   private static <T> T createProxy(Class<T> itf, String lang) throws IOException
   {
      InvocationHandler ih;
      if (GenericX.isA(itf, Messages.class))
      {
         ih = new GenericMessages(itf, lang);
      }
      else
      {
         throw new InvalidParameterException("Class " + itf.getName() + " is not a GWT i18n subclass");
      }
      return (T) Proxy.newProxyInstance(itf.getClassLoader(), new Class[] { itf }, ih);
   }
}