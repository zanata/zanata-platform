package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.view.NeedsRefresh;
import com.google.gwt.user.client.ui.HasText;

public interface SourceContentWrapper extends HasText, NeedsRefresh {
    void setTitle(String title);

    void highlight(String searchTerm);
}
