package org.zanata.webtrans.server.locale;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.i18n.client.LocalizableResource.Key;
import com.google.gwt.i18n.client.Messages.DefaultMessage;
import com.google.gwt.i18n.client.Messages.PluralCount;
import com.google.gwt.i18n.client.Messages.AlternateMessage;
import com.google.gwt.i18n.client.PluralRule;

public class GenericMessages extends GenericX
{

   public GenericMessages(Class<?> _itf, String lang) throws IOException, InvalidParameterException
   {
      super(_itf, lang);
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if (!String.class.equals(method.getReturnType()))
      {
         return "Invalid return type of the method " + method.toString();
      }
      // first, try to use the annotation if there is one.
      Key k = method.getAnnotation(Key.class);
      String result = null;
      if (k != null)
      {
         result = buildMessage(k.value(), method, args);
      }
      if (result == null)
      {
         result = buildMessage(method.getName(), method, args);
      }
      return result;
   }

   private String buildMessage(String propertyName, Method method, Object[] args) throws Throwable
   {
      AlternateMessage pluralTextAnnotation = method.getAnnotation(AlternateMessage.class);
      Map<Integer, String> pluralParamIndex2pattern = new HashMap<Integer, String>();
      String pluralKey = "";
      Map<String, String> pluralKey2defaultValue = new HashMap<String, String>();
      if (pluralTextAnnotation != null)
      {
         String[] pairs = pluralTextAnnotation.value();
         for (int i = 0; (i + 1) < pairs.length; i += 2)
         {
            pluralKey2defaultValue.put(pairs[i], pairs[i + 1]);
         }
      }
      Annotation[][] paramsAnnotations = method.getParameterAnnotations();
      Class<?>[] paramTypes = method.getParameterTypes();
      for (int i = 0; i < paramTypes.length; i++)
      {
         Class<?> paramType = paramTypes[i];
         PluralCount pc = getAnnotation(paramsAnnotations[i], PluralCount.class);
         if (pc == null)
         {
            continue;
         }
         else if (pc.value() != PluralRule.class)
         {
            // user plural rule is not supported
         }
         else
         {
            // manage plural text
            // TODO manage primitive types
            int n = -1;
            if (isA(paramType, Number.class) || isA(paramType, byte.class) || isA(paramType, short.class) || isA(paramType, int.class) || isA(paramType, long.class))
            {
               n = ((Number) args[i]).intValue();
            }
            if (n == 0)
            {
               pluralKey += "[none]";
               pluralParamIndex2pattern.put(i, null);
            }
            else if (n == 1)
            {
               pluralKey += "[one]";
               pluralParamIndex2pattern.put(i, null);
            }
            else if (n == 2)
            {
               pluralKey += "[two]";
               pluralParamIndex2pattern.put(i, null);
            }
            else if (3 <= n && n <= 10)
            {
               pluralKey += "[few]";
            }
            else if (11 <= n && n <= 99)
            {
               pluralKey += "[many]";
            }
            pluralParamIndex2pattern.put(i, "\\{" + i + ",number\\}");
         }
      }
      String template = properties.getProperty(propertyName + pluralKey);
      if (template == null)
      {
         DefaultMessage dm = method.getAnnotation(DefaultMessage.class);
         if (dm == null)
         {
         }
         else
         {
            template = dm.value();
         }
         if (template == null)
         {
            return null;
         }
      }
      assert template != null;

      if (args != null)
      {
         for (int i = 0; i < args.length; i++)
         {
            String value = args[i] == null ? "null" : args[i].toString();
            String replacedPattern = null;
            if (pluralParamIndex2pattern.containsKey(i))
            {
               replacedPattern = pluralParamIndex2pattern.get(i);
            }
            else
            {
               replacedPattern = "\\{" + i + "\\}";
            }
            if (replacedPattern != null)
            {
               template = template.replaceAll(replacedPattern, value == null ? "" : value);
            }
         }
      }
      return template;
   }

   @SuppressWarnings("unchecked")
   <T extends Annotation> T getAnnotation(Annotation[] as, Class<T> annotation)
   {
      for (Annotation a : as)
      {
         if (isA(a.getClass(), annotation))
         {
            return (T) a;
         }
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   <T extends Annotation> List<T> getAnnotations(Annotation[] as, Class<T> a)
   {
      List<T> foundAnnotations = new ArrayList<T>();
      for (Annotation _a : as)
      {
         if (_a.getClass().equals(a.getClass()))
         {
            foundAnnotations.add((T) _a);
         }
      }
      return foundAnnotations;
   }
}