package org.zanata.client.commands.gettext;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
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

    public static Path[] findLocaleDirs(Path srcDir) {
        Path[] localeDirs;
        localeDirs = srcDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(Path f) {
                return f.isDirectory() && !f.getName().equals("pot");
            }
        });
        return localeDirs;
    }

    public static List<LocaleMapping> findLocales(Path srcDir) {
        Path[] localeDirs = findLocaleDirs(srcDir);
        List<LocaleMapping> locales = new ArrayList<LocaleMapping>();
        for (Path dir : localeDirs) {
            locales.add(new LocaleMapping(dir.getName()));
        }
        return locales;
    }

    public static List<LocaleMapping> findLocales(Path srcDir,
                                                  LocaleList locales) {
        List<LocaleMapping> localeDirs = new ArrayList<LocaleMapping>();

        for (LocaleMapping loc : locales) {
            Path localeDir = new Path(srcDir, loc.getLocalLocale());
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
