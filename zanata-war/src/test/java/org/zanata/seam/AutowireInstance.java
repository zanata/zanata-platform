package org.zanata.seam;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import com.google.common.collect.Lists;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;

/**
* @author Patrick Huang
*         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
@Exclude(ifProjectStage = ProjectStage.IntegrationTest.class)
class AutowireInstance implements Instance {
    private final Object value;

    public AutowireInstance(Object value) {
        this.value = value;
    }

    @Override
    public Iterator iterator() {
        return Lists.newArrayList(value).iterator();
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
