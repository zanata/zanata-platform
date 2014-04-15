package org.zanata.ui.autocomplete;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.jboss.seam.Component;
import org.zanata.model.HLocale;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.ui.AbstractAutocomplete;
import org.zanata.ui.FilterUtil;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class LocaleAutocomplete extends AbstractAutocomplete<HLocale> {
    protected LocaleService localeServiceImpl = (LocaleService) Component
            .getInstance(LocaleServiceImpl.class);

    protected List<HLocale> supportedLocales = localeServiceImpl
            .getSupportedLocales();

    protected abstract Collection<HLocale> getLocales();

    /**
     * Return results on search
     */
    @Override
    public List<HLocale> suggest() {
        final Collection<HLocale> entityLocales = getLocales();

        Collection<HLocale> filtered =
                Collections2.filter(supportedLocales, new Predicate<HLocale>() {
                    @Override
                    public boolean apply(HLocale input) {
                        return FilterUtil.isIncludeLocale(entityLocales, input,
                                getQuery());
                    }
                });
        return Lists.newArrayList(filtered);
    }
}
