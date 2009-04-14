package org.jboss.shotoku.fileaccess;

import java.io.InputStream;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class ContentInformation {
    private String mimeType;
    private long contentLenght;
    private InputStream is;

    public ContentInformation(String mimeType, long contentLenght, InputStream is) {
        this.mimeType = mimeType;
        this.contentLenght = contentLenght;
        this.is = is;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getContentLenght() {
        return contentLenght;
    }

    public InputStream getIs() {
        return is;
    }

    public boolean isResponseDone() {
        return false;
    }
}
