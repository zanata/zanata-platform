package net.openl10n.flies.webtrans.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the entire application.
 */
public interface Resources extends ClientBundle
{

   @Source("net/openl10n/flies/webtrans/WebTransStyles.css")
   WebTransStyles style();

   @Source("net/openl10n/flies/webtrans/images/banner_bg.png")
   DataResource bannerBackground();

   @Source("net/openl10n/flies/webtrans/images/flies_logo_small.png")
   ImageResource logo();

   @Source("net/openl10n/flies/webtrans/images/x.png")
   ImageResource xButton();

   @Source("net/openl10n/flies/webtrans/images/x2.png")
   ImageResource minimizeButton();

   @Source("net/openl10n/flies/webtrans/images/silk/user.png")
   ImageResource userOnline();

   @Source("net/openl10n/flies/webtrans/images/silk/page_white_text.png")
   ImageResource documentImage();

   @Source("net/openl10n/flies/webtrans/images/silk/folder.png")
   ImageResource folderImage();

   @Source("net/openl10n/flies/webtrans/images/silk/information.png")
   ImageResource informationImage();

   @Source("net/openl10n/flies/webtrans/images/next_entry.png")
   ImageResource nextEntry();

   @Source("net/openl10n/flies/webtrans/images/prev_entry.png")
   ImageResource prevEntry();

   @Source("net/openl10n/flies/webtrans/images/next_fuzzy.png")
   ImageResource nextFuzzy();

   @Source("net/openl10n/flies/webtrans/images/prev_fuzzy.png")
   ImageResource prevFuzzy();

   @Source("net/openl10n/flies/webtrans/images/next_untranslated.png")
   ImageResource nextUntranslated();

   @Source("net/openl10n/flies/webtrans/images/prev_untranslated.png")
   ImageResource prevUntranslated();

   @Source("net/openl10n/flies/webtrans/images/next_approved.png")
   ImageResource nextApproved();

   @Source("net/openl10n/flies/webtrans/images/prev_approved.png")
   ImageResource prevApproved();

   @Source("net/openl10n/flies/webtrans/images/tm_view.png")
   ImageResource tmViewButton();

   @Source("net/openl10n/flies/webtrans/images/collapse_open.png")
   ImageResource collapseOpen();

   @Source("net/openl10n/flies/webtrans/images/collapse_closed.png")
   ImageResource collapseClosed();

   @Source("net/openl10n/flies/webtrans/images/first_page.png")
   ImageResource firstPageImage();

   @Source("net/openl10n/flies/webtrans/images/prev_page.png")
   ImageResource prevPageImage();

   @Source("net/openl10n/flies/webtrans/images/next_page.png")
   ImageResource nextPageImage();

   @Source("net/openl10n/flies/webtrans/images/last_page.png")
   ImageResource lastPageImage();

   @Source("net/openl10n/flies/webtrans/images/first_page_disabled.png")
   ImageResource firstPageDisabledImage();

   @Source("net/openl10n/flies/webtrans/images/prev_page_disabled.png")
   ImageResource prevPageDisabledImage();

   @Source("net/openl10n/flies/webtrans/images/next_page_disabled.png")
   ImageResource nextPageDisabledImage();

   @Source("net/openl10n/flies/webtrans/images/last_page_disabled.png")
   ImageResource lastPageDisabledImage();

   // unused after intro of grey-out icons to reflect disabled state
   @Source("net/openl10n/flies/webtrans/images/no_page.png")
   ImageResource noPageImage();

   @Source("net/openl10n/flies/webtrans/images/approved_unit.png")
   ImageResource approvedUnit();

   @Source("net/openl10n/flies/webtrans/images/error_unit.png")
   ImageResource errorUnit();

   @Source("net/openl10n/flies/webtrans/images/fuzzy_unit.png")
   ImageResource fuzzyUnit();

   @Source("net/openl10n/flies/webtrans/images/new_unit.png")
   ImageResource newUnit();

}