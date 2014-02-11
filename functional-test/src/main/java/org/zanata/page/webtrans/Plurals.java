package org.zanata.page.webtrans;

public enum Plurals {
    // source plural forms
    SourceSingular(0), SourcePlural(1),
    // target plural forms
    TargetSingular(0), TargetPluralOne(1), TargetPluralTwo(2),
    TargetPluralThree(3), TargetPluralFour(4), TargetPluralFive(5);
    private final int index;

    Plurals(int index) {
        this.index = index;
    }

    int index() {
        return index;
    }
}
