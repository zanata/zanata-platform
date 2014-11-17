package org.zanata.client.commands.gettext;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;

/**
 *
 * @author Sean Flanigan &lt;<a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>&gt;
 *
 */
public class PublicanUtil {
    private static final Logger log = LoggerFactory
            .getLogger(PublicanUtil.class);

    private PublicanUtil() {
    }

    public static File[] findLocaleDirs(File srcDir) {
        File[] localeDirs;
        localeDirs = srcDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() && !f.getName().equals("pot");
            }
        });
        return localeDirs;
    }

    public static List<LocaleMapping> findLocales(File srcDir) {
        File[] localeDirs = findLocaleDirs(srcDir);
        List<LocaleMapping> locales = new ArrayList<LocaleMapping>();
        for (File dir : localeDirs) {
            locales.add(new LocaleMapping(dir.getName()));
        }
        return locales;
    }

    public static List<LocaleMapping> findLocales(File srcDir,
            LocaleList locales) {
        List<LocaleMapping> localeDirs = new ArrayList<LocaleMapping>();

        for (LocaleMapping loc : locales) {
            File localeDir = new File(srcDir, loc.getLocalLocale());
            if (localeDir.isDirectory())
                localeDirs.add(loc);
            else
                log.warn(
                        "configured locale {} not found; directory {} does not exist",
                        loc.getLocale(), loc.getLocalLocale());
        }

        return localeDirs;
    }
}
