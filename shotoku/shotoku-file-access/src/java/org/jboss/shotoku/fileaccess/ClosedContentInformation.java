package org.jboss.shotoku.fileaccess;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class ClosedContentInformation extends ContentInformation {
    public ClosedContentInformation() {
        super(null, 0, null);
    }

    public boolean isResponseDone() {
        return true;
    }
}
