package org.zanata.adapter.asciidoc;

import net.sf.okapi.common.filters.EventBuilder;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class AsciidocEventBuilder extends EventBuilder {
    private InlineCodeFinder codeFinder;

    public AsciidocEventBuilder(String rootId, IFilter subFilter) {
        super(rootId, subFilter);
        codeFinder = null;
    }

    @Override
    protected ITextUnit postProcessTextUnit(ITextUnit textUnit) {
        TextFragment text = textUnit.getSource().getFirstContent();
        if (codeFinder != null) {
            codeFinder.process(text);
        }
        return textUnit;
    }

    public void setCodeFinder(InlineCodeFinder codeFinder) {
        this.codeFinder = codeFinder;
    }
}
