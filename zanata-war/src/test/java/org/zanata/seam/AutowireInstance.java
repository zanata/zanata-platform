package org.zanata.seam;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

/**
* @author Patrick Huang
*         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
class AutowireInstance implements Instance {
    private final Object value;

    public AutowireInstance(Object value) {
        this.value = value;
    }

    @Override
    public Iterator iterator() {
        throw new UnsupportedOperationException(
                "SeamAutowire doesn't support this");
    }

    @Override
    public Instance select(Annotation... annotations) {
        throw new UnsupportedOperationException(
                "SeamAutowire doesn't support this");
    }

    @Override
    public boolean isUnsatisfied() {
        throw new UnsupportedOperationException(
                "SeamAutowire doesn't support this");
    }

    @Override
    public boolean isAmbiguous() {
        throw new UnsupportedOperationException(
                "SeamAutowire doesn't support this");
    }

    @Override
    public Instance select(TypeLiteral typeLiteral,
            Annotation... annotations) {
        throw new UnsupportedOperationException(
                "SeamAutowire doesn't support this");
    }

    @Override
    public Instance select(Class aClass,
            Annotation... annotations) {
        throw new UnsupportedOperationException(
                "SeamAutowire doesn't support this");
    }

    @Override
    public Object get() {
        return value;
    }
}
