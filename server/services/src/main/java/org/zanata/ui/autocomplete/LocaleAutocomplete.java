package org.zanata.ui.autocomplete;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.zanata.model.HLocale;
import org.zanata.service.LocaleService;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.ui.AbstractAutocomplete;
import org.zanata.util.ServiceLocator;

import static org.zanata.ui.FilterUtil.isIncludeLocale;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class LocaleAutocomplete extends AbstractAutocomplete<HLocale> {
    private static final long serialVersionUID = -540662034050685697L;
    protected LocaleService localeServiceImpl = ServiceLocator.instance()
            .getInstance(LocaleServiceImpl.class);

    private List<HLocale> supportedLocales = localeServiceImpl
            .getSupportedLocales();

    protected abstract Collection<HLocale> getLocales();

    /**
     * Return results on search
     */
    @Override
    public List<HLocale> suggest() {
        final Collection<HLocale> entityLocales = getLocales();
        return supportedLocales.stream()
                .filter(it ->
                        isIncludeLocale(entityLocales, it, getQuery()))
                .collect(Collectors.toList());
    }
}
