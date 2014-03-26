package org.zanata.ui.autocomplete;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
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

    protected abstract Collection<HLocale> getLocales();

    protected abstract void updateInstanceList(HLocale hLocale);

    protected abstract void update();

    protected abstract void displaySuccessfulMessage(String localeDisplayName);

    /**
     * Return results on search
     */
    @Override
    public List<HLocale> suggest() {
        List<HLocale> localeList = localeServiceImpl.getSupportedLocales();

        Collection<HLocale> filtered =
                Collections2.filter(localeList, new Predicate<HLocale>() {
                    @Override
                    public boolean apply(@Nullable HLocale input) {
                        return FilterUtil.isIncludeLocale(getLocales(), input,
                                getQuery());
                    }
                });
        return Lists.newArrayList(filtered);
    }

    @Override
    public void onSelectItemAction() {
        if (StringUtils.isEmpty(getSelectedItem())) {
            return;
        }

        HLocale locale = localeServiceImpl.getByLocaleId(getSelectedItem());

        updateInstanceList(locale);

        update();
        reset();
        displaySuccessfulMessage(locale.retrieveDisplayName());

    }
}
