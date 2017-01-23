package org.zanata.action;

import java.io.Serializable;

/**
 * Stores options for the lucene indexing
 *
 * @author David Mason, damason@redhat.com
 */
public class ReindexClassOptions implements Serializable {
    private static final long serialVersionUID = 1L;
    private Class<?> clazz;
    private boolean purge = false;
    private boolean reindex = false;
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

    public boolean isPurge() {
        return this.purge;
    }

    public void setPurge(final boolean purge) {
        this.purge = purge;
    }

    public boolean isReindex() {
        return this.reindex;
    }

    public void setReindex(final boolean reindex) {
        this.reindex = reindex;
    }

    public boolean isOptimize() {
        return this.optimize;
    }

    public void setOptimize(final boolean optimize) {
        this.optimize = optimize;
    }
}
