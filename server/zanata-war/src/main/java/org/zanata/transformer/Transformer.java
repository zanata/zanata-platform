package org.zanata.transformer;

@FunctionalInterface
public interface Transformer<F, T> {
    boolean transform(F from, T to);
}
