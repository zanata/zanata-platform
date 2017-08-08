package org.zanata.webtrans.server.locale;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Properties;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.server.GwtLocaleFactoryImpl;
import com.google.gwt.i18n.server.impl.ReflectionMessage;
import com.google.gwt.i18n.server.impl.ReflectionMessageInterface;
import com.google.gwt.i18n.shared.GwtLocaleFactory;

public abstract class GenericProxy implements InvocationHandler {
    protected final Properties properties = new Properties();
    protected final Class<?> itf;
    private static final GwtLocaleFactory gwtLocaleFactory =
            new GwtLocaleFactoryImpl();
    private final ReflectionMessageInterface messageInterface;

    public abstract Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable;

    public GenericProxy(Class<? extends LocalizableResource> _itf, String lang)
            throws IOException, InvalidParameterException {
        this.itf = _itf;
        messageInterface =
                new ReflectionMessageInterface(gwtLocaleFactory, _itf);
        fillProperties(itf, lang);
    }

    protected void fillProperties(Class<?> itf, String lang) throws IOException {
        for (Class<?> superItf : itf.getInterfaces()) {
            fillProperties(superItf, lang);
        }
        String suffix = lang == null ? "" : ("_" + lang);
        String baseName = itf.getName().replace('.', '/');
        InputStream in = load(baseName + suffix + ".properties");
        if (in == null) {
            in = load(baseName + ".properties");
        }
        if (in != null) {
            properties.load(in);
        }
    }

    protected InputStream load(String s) {
        InputStream in = null;
        ClassLoader cl;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            in = cl.getResourceAsStream(s);
        }
        if (in == null) {
            cl = getClass().getClassLoader();
            if (cl != null) {
                in = getClass().getClassLoader().getResourceAsStream(s);
            }
            if (in == null) {
                cl = ClassLoader.getSystemClassLoader();
                if (cl != null) {
                    in = cl.getResourceAsStream(s);
                }
            }
        }
        return in;
    }

    protected String getKey(Method method) {
        return new ReflectionMessage(gwtLocaleFactory, messageInterface, method)
                .getKey();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public int hashCode() {
        return properties.size();
    }

    public static boolean isA(Class<?> c1, Class<?> c2) {
        return c2.isAssignableFrom(c1);
    }
}
