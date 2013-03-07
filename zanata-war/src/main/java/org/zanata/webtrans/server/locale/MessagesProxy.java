package org.zanata.webtrans.server.locale;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.LocalizableResource.Key;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.PluralRule;

public class MessagesProxy extends GenericProxy
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
      String pluralKey = "";
      Map<Integer, String> paramIndexpattern = new HashMap<Integer, String>();

      for (int i = 0; i < desc.args.length; i++)
      {
         MessageArgument arg = desc.args[i];
         String pattern = null;
         if (arg.pluralCount)
         {
            int n = 0;
            if (isA(args[i].getClass(), Number.class))
            {
               n = ((Number) args[i]).intValue();
               pattern = "\\{" + i + ",number\\}";
            }
            else if (isA(args[i].getClass(), List.class))
            {
               n = ((List) args[i]).size();
               pattern = "\\{" + i + ",list,string\\}";
            }
            else if (isA(args[i].getClass(), Collection.class))
            {
               n = ((Collection) args[i]).size();
               pattern = "\\{" + i + ",collection,string\\}";
            }

            pluralKey += NumberUtils.getWords(n - arg.pluralOffset) + " ";

         }
         else
         {
            pattern = "\\{" + i + "\\}";
         }
         paramIndexpattern.put(i, pattern);

         /**
          * Not supported
          * 
          * if (arg.select) { Object value = args[i]; String select; if (value
          * instanceof Enum) { select = ((Enum) value).name(); } else { select =
          * String.valueOf(value); } }
          */
      }
      
      String template = desc.defaults.get(pluralKey);

      if (template == null)
      {
         template = desc.defaults.get("");
      }

      if (args != null)
      {
         for (int i = 0; i < args.length; i++)
         {
            String value = args[i] == null ? "null" : args[i].toString();
            String replacedPattern = null;

            replacedPattern = paramIndexpattern.get(i);

            if (replacedPattern != null)
            {
               template = template.replaceAll(replacedPattern, value == null ? "" : value);
            }
         }
      }

      return template;
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