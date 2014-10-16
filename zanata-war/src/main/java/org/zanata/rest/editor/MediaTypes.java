package org.zanata.rest.editor;

public class MediaTypes {

    public static enum Format {
        JSON("json");

        private final String format;

        private Format(String format) {
            this.format = format;
        }

        public String toString() {
            return "+" + format;
        };
    }

    private static final String JSON = "+json";

    private static final String APPLICATION_VND_ZANATA =
            "application/vnd.zanata";

    public static final String APPLICATION_ZANATA_PROJECT =
            APPLICATION_VND_ZANATA + ".project";

    public static final String APPLICATION_ZANATA_PROJECT_JSON =
            APPLICATION_ZANATA_PROJECT + JSON;

    public static final String APPLICATION_ZANATA_LOCALES =
            APPLICATION_VND_ZANATA + ".locales";
    public static final String APPLICATION_ZANATA_LOCALES_JSON =
            APPLICATION_ZANATA_LOCALES + JSON;

    public static final String APPLICATION_ZANATA_PROJECT_VERSION =
            APPLICATION_VND_ZANATA + ".version";
    public static final String APPLICATION_ZANATA_PROJECT_VERSION_JSON =
            APPLICATION_ZANATA_PROJECT_VERSION + JSON;

    public static final String APPLICATION_ZANATA_VERSION_LOCALES =
            APPLICATION_ZANATA_PROJECT_VERSION + ".locales";
    public static final String APPLICATION_ZANATA_VERSION_LOCALES_JSON =
            APPLICATION_ZANATA_VERSION_LOCALES + JSON;

    public static final String APPLICATION_ZANATA_USER =
            APPLICATION_VND_ZANATA + ".user";
    public static final String APPLICATION_ZANATA_USER_JSON =
            APPLICATION_ZANATA_USER + JSON;

    public static final String APPLICATION_ZANATA_TRANSLATION_DATA_JSON =
            APPLICATION_VND_ZANATA + ".translation.data" + JSON;

    public static final String APPLICATION_ZANATA_TRANS_UNIT =
            APPLICATION_VND_ZANATA + ".tu";

    public static final String APPLICATION_ZANATA_TRANS_UNIT_RESOURCE_JSON =
            APPLICATION_ZANATA_TRANS_UNIT + ".resource" + JSON;

    public static final String APPLICATION_ZANATA_SOURCE_JSON =
            APPLICATION_ZANATA_TRANS_UNIT + ".source" + JSON;

    public static final String APPLICATION_ZANATA_TRANSLATION_JSON =
            APPLICATION_ZANATA_TRANS_UNIT + ".translation" + JSON;

    public static final String APPLICATION_ZANATA_TRANS_UNIT_JSON =
            APPLICATION_ZANATA_TRANS_UNIT + JSON;
}
