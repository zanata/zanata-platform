package org.zanata.action;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Stores options for the lucene indexing
 *
 * @author David Mason, damason@redhat.com
 */
public class ReindexClassOptions implements Serializable {
    private static final long serialVersionUID = 1L;
    private Class<?> clazz;

    @Getter
    @Setter
    private boolean purge = false;

    @Getter
    @Setter
    private boolean reindex = false;

    @Getter
    @Setter
    private boolean optimize = false;

    public ReindexClassOptions(Class<?> indexableClass) {
        clazz = indexableClass;
    }

    public String getClassName() {
        return clazz.getSimpleName();
    }

    public void setSelectAll(boolean selectAll) {
        setPurge(selectAll);
        setReindex(selectAll);
        setOptimize(selectAll);
    }

    /**
     * Returns true only if all other boolean properties (purge, reindex,
     * optimize) are true
     *
     * @return
     */
    public boolean getSelectAll() {
        return isPurge() && isReindex() && isOptimize();
    }

}
