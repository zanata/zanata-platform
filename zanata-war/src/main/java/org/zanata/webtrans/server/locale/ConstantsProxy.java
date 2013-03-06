package org.zanata.webtrans.server.locale;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.Constants.DefaultBooleanValue;
import com.google.gwt.i18n.client.Constants.DefaultDoubleValue;
import com.google.gwt.i18n.client.Constants.DefaultFloatValue;
import com.google.gwt.i18n.client.Constants.DefaultIntValue;
import com.google.gwt.i18n.client.Constants.DefaultStringArrayValue;
import com.google.gwt.i18n.client.Constants.DefaultStringMapValue;
import com.google.gwt.i18n.client.Constants.DefaultStringValue;
import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.LocalizableResource.Key;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ConstantsProxy extends GenericX {

   public ConstantsProxy(Class<? extends LocalizableResource> _itf, String lang) throws IOException, InvalidParameterException
   {
        super(_itf, lang);
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getParameterTypes().length == 1 
                && isA(method.getParameterTypes()[0], String.class)
                && args.length == 1 && args[0] instanceof String
                && isMethodOf(method, ConstantsWithLookup.class)) {
            return invokeGenericMethod(proxy, (String) args[0], method.getReturnType()); 
        } else if (method.getParameterTypes().length != 0) {
            return null;
        }
        Key k = method.getAnnotation(Key.class);
        Object result = null;
        if (k != null) {
            result = buildConstant(k.value(), method);
        }
        if (result == null) {
            result = buildConstant(method.getName(), method);
        }
        return result;
    }
    
    private boolean isMethodOf(Method method, Class<?> clazz) {
        try {
            return clazz.getMethod(method.getName(), method.getParameterTypes()) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    private Object invokeGenericMethod(Object proxy, String methodName, Class<?> returnType) throws Throwable{
        try {
            //try to invoke the method on the proxy
            return itf.getMethod(methodName, new Class<?>[0]).invoke(proxy, new Object[0]);
        } catch (Throwable e) {
            //use the method name as property name
            String resultAsString = properties.getProperty(methodName);
            if (resultAsString == null) {
                return null;
            } else {
                //Convert string value under the required Type
                return toObject(resultAsString, returnType);
            }
        }
    }
    
    private Object buildConstant(String propertyName, Method method) {
        String resultAsString = properties.getProperty(propertyName);
        if (resultAsString == null) {
            //use the default value (annotation) if there is one
            return getDefaultValue(method);
        } else {
            //Convert string value under the required Type
            return toObject(resultAsString, method.getReturnType());
        }
    }
    
    private Object getDefaultValue(Method method) {
        final Class<?> returnClass = method.getReturnType();
        if (returnClass.equals(String.class)) {
            DefaultStringValue a = method.getAnnotation(DefaultStringValue.class);
            if (a != null) {
                return a.value();
            }
        
        } else if (returnClass.equals(Integer.class) || returnClass.equals(int.class)) {
            DefaultIntValue a = method.getAnnotation(DefaultIntValue.class);
            if (a != null) {
                return a.value();
            }
        
        } else if (returnClass.equals(Float.class) || returnClass.equals(float.class)) {
            DefaultFloatValue a = method.getAnnotation(DefaultFloatValue.class);
            if (a != null) {
                return a.value();
            }
            
        } else if (returnClass.equals(Double.class) || returnClass.equals(double.class)) {
            DefaultDoubleValue a = method.getAnnotation(DefaultDoubleValue.class);
            if (a != null) {
                return a.value();
            }
            
        } else if (returnClass.equals(Boolean.class) || returnClass.equals(boolean.class)) {
            DefaultBooleanValue a = method.getAnnotation(DefaultBooleanValue.class);
            if (a != null) {
                return a.value();
            }

        } else if (isStringMap(returnClass)) {
            DefaultStringMapValue a = method.getAnnotation(DefaultStringMapValue.class);
            if (a != null) {
                Map<String,String> result = new HashMap<String,String>();
                String[] s = a.value();
                for(int i=0; (i+1)<s.length; i+=2) {
                    result.put(s[i], s[i+1]);
                }
                return result;
            }

        } else if (isStringArray(returnClass)) {
            DefaultStringArrayValue a = method.getAnnotation(DefaultStringArrayValue.class);
            if (a != null) {
                return a.value();
            }
        }
        //No value found !
        return null;
    }
    
    private Object toObject(String resultAsString, Class<?> clazz) {
        if (clazz.equals(String.class)) {
            return resultAsString;
        
        } else if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
            return Integer.valueOf(resultAsString);
        
        } else if (clazz.equals(Float.class) || clazz.equals(float.class)) {
            return Float.valueOf(resultAsString);
            
        } else if (clazz.equals(Double.class) || clazz.equals(double.class)) {
            return Double.valueOf(resultAsString);
            
        } else if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
            return Boolean.valueOf(resultAsString);

        } else if (isStringMap(clazz)) {
            Map<String,String> result = new HashMap<String,String>();
            StringTokenizer st = new StringTokenizer(resultAsString, ",", false);
            while(st.hasMoreTokens()) {
                String key = st.nextToken();
                String value = properties.getProperty(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
            
            return result;

        } else if (isStringArray(clazz)) {
            return resultAsString.split(",");
        } else {
            return null;
        }
    }
    
    public boolean isStringArray(Class<?> c) {
        return c.isArray() && String.class.equals(c.getComponentType());
        
    }
    public boolean isStringMap(Class<?> c) {
        return isA(c, Map.class);
    }
}