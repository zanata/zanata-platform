package org.zanata.transformer;

public interface Transformer<F, T> {
    boolean transform(F from, T to);
}
