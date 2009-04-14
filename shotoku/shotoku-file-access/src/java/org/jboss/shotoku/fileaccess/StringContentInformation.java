package org.jboss.shotoku.fileaccess;

import java.io.ByteArrayInputStream;

/**
 * @author <a href="mailto:adamw@aster.pl">Adam Warski</a>
 */
public class StringContentInformation extends ContentInformation {
    public StringContentInformation(String content) {
        super("text/html", content.getBytes().length, new ByteArrayInputStream(content.getBytes()));
    }
}
