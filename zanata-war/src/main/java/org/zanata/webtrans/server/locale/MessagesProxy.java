package org.zanata.webtrans.server.locale;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.LocalizableResource.Key;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.Messages.AlternateMessage;
import com.google.gwt.i18n.client.Messages.DefaultMessage;
import com.google.gwt.i18n.client.Messages.PluralCount;
import com.google.gwt.i18n.client.PluralRule;

public class MessagesProxy extends GenericX
{
   public MessagesProxy(Class<? extends LocalizableResource> _itf, String lang) throws IOException, InvalidParameterException
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
      MessageDescriptor desc = getDescriptor(method);
      List<String> forms = new ArrayList<String>();

      for (int i = 0; i < desc.args.length; i++)
      {
         MessageArgument arg = desc.args[i];
         if (arg.pluralCount)
         {
            PluralRule rule = arg.pluralRule.newInstance();

            int n = 0;
            if (isA(args[i].getClass(), Number.class))
            {
               n = ((Number) args[i]).intValue();
            }
            else if (isA(args[i].getClass(), List.class))
            {
               n = ((List) args[i]).size();
            }
            forms = expand(forms, "=" + n, rule.pluralForms()[rule.select(n - arg.pluralOffset)].getName());
            args[i] = n - arg.pluralOffset;
         }
         if (arg.select)
         {
            Object value = args[i];
            String select;
            if (value instanceof Enum)
            {
               select = ((Enum) value).name();
            }
            else
            {
               select = String.valueOf(value);
            }
            // forms = expand(forms, select, "other");
         }
      }

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
         else if (pc.value() == PluralRule.class)
         {
            // manage plural text
            int n = -1;
            if (isA(paramType, Number.class) || isA(paramType, byte.class) || isA(paramType, short.class) || isA(paramType, int.class) || isA(paramType, long.class))
            {
               n = ((Number) args[i]).intValue();
            }
            else if (isA(paramType, List.class))
            {
               n = ((List) args[i]).size();
            }

            if (n == 0)
            {
               pluralKey += "[none]";
            }
            else if (n == 1)
            {
               pluralKey += "[one]";
            }
            else if (n == 2)
            {
               pluralKey += "[two]";
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
         if (dm != null)
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
   private <T extends Annotation> T getAnnotation(Annotation[] as, Class<T> annotation)
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
   private <T extends Annotation> List<T> getAnnotations(Annotation[] as, Class<T> a)
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

   private static List<String> expand(List<String> list, String... variants)
   {
      if (list.isEmpty())
      {
         return new ArrayList<String>(Arrays.asList(variants));
      }
      List<String> result = new ArrayList<String>(list.size() * variants.length);
      for (String str : list)
      {
         for (String variant : variants)
         {
            result.add(str + '|' + variant);
         }
      }
      return result;
   }

   protected MessageDescriptor getDescriptor(Method method)
   {
      MessageDescriptor desc = new MessageDescriptor();
      Messages.DefaultMessage defaultMessageAnnotation = method.getAnnotation(Messages.DefaultMessage.class);
      if (defaultMessageAnnotation != null)
      {
         desc.defaults.put("", defaultMessageAnnotation.value());
      }

      desc.key = getKey(method);
      String[] defaults = null;
      Messages.AlternateMessage alternateMessageAnnotation = method.getAnnotation(Messages.AlternateMessage.class);
      if (alternateMessageAnnotation != null)
      {
         defaults = alternateMessageAnnotation.value();
      }

      if (defaults != null)
      {
         for (int i = 0; (i + 1) < defaults.length; i += 2)
         {
            desc.defaults.put(defaults[i], defaults[i + 1]);
         }
      }
      Annotation[][] args = method.getParameterAnnotations();
      desc.args = new MessageArgument[args.length];
      for (int i = 0; i < args.length; i++)
      {
         desc.args[i] = new MessageArgument();
         for (Annotation annotation : args[i])
         {
            if (annotation instanceof Messages.PluralCount)
            {
               desc.args[i].pluralCount = true;
               desc.args[i].pluralRule = ((Messages.PluralCount) annotation).value();
            }
            if (annotation instanceof Messages.Offset)
            {
               desc.args[i].pluralOffset = ((Messages.Offset) annotation).value();
            }
            if (annotation instanceof Messages.Select)
            {
               desc.args[i].select = true;
            }
         }
      }
      return desc;
   }

   class MessageDescriptor
   {
      String key;
      Map<String, String> defaults = new HashMap<String, String>();
      MessageArgument[] args;
   }

   class MessageArgument
   {
      boolean pluralCount;
      int pluralOffset;
      Class<? extends PluralRule> pluralRule;
      boolean select;
   }
}