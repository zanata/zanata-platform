package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the entire application.
 */
public interface Resources extends ClientBundle {

	@Source("org/fedorahosted/flies/webtrans/WebTransStyles.css")
	WebTransStyles style();
	
	@Source("org/fedorahosted/flies/webtrans/images/banner_bg.png")
	DataResource bannerBackground();

	@Source("org/fedorahosted/flies/webtrans/images/flies_logo_small.png")
	ImageResource logo();
	
	@Source("org/fedorahosted/flies/webtrans/images/x.png")
	ImageResource xButton();

	@Source("org/fedorahosted/flies/webtrans/images/x2.png")
	ImageResource minimizeButton();
	
	@Source("org/fedorahosted/flies/webtrans/images/silk/user.png")
	ImageResource userOnline();

	@Source("org/fedorahosted/flies/webtrans/images/silk/page_white_text.png")
	ImageResource documentImage();

	@Source("org/fedorahosted/flies/webtrans/images/silk/folder.png")
	ImageResource folderImage();
	
	@Source("org/fedorahosted/flies/webtrans/images/silk/information.png")
	ImageResource informationImage();
	
	@Source("org/fedorahosted/flies/webtrans/images/next_entry.png")
	ImageResource nextEntry();

	@Source("org/fedorahosted/flies/webtrans/images/prev_entry.png")
	ImageResource prevEntry();

	@Source("org/fedorahosted/flies/webtrans/images/next_fuzzy.png")
	ImageResource nextFuzzy();
	
	@Source("org/fedorahosted/flies/webtrans/images/prev_fuzzy.png")
	ImageResource prevFuzzy();

	@Source("org/fedorahosted/flies/webtrans/images/next_untranslated.png")
	ImageResource nextUntranslated();
	
	@Source("org/fedorahosted/flies/webtrans/images/prev_untranslated.png")
	ImageResource prevUntranslated();

	@Source("org/fedorahosted/flies/webtrans/images/next_approved.png")
	ImageResource nextApproved();

	@Source("org/fedorahosted/flies/webtrans/images/prev_approved.png")
	ImageResource prevApproved();
	
	@Source("org/fedorahosted/flies/webtrans/images/tm_view.png")
	ImageResource tmViewButton();
	
	@Source("org/fedorahosted/flies/webtrans/images/collapse_open.png")
	ImageResource collapseOpen();
	
	@Source("org/fedorahosted/flies/webtrans/images/collapse_closed.png")
	ImageResource collapseClosed();
	
	@Source("org/fedorahosted/flies/webtrans/images/first_page.png")
	ImageResource firstPageImage();
	
	@Source("org/fedorahosted/flies/webtrans/images/prev_page.png")
	ImageResource prevPageImage();
	
	@Source("org/fedorahosted/flies/webtrans/images/next_page.png")
	ImageResource nextPageImage();
	
	@Source("org/fedorahosted/flies/webtrans/images/last_page.png")
	ImageResource lastPageImage();
	
	@Source("org/fedorahosted/flies/webtrans/images/first_page_disabled.png")
	ImageResource firstPageDisabledImage();
	
	@Source("org/fedorahosted/flies/webtrans/images/prev_page_disabled.png")
	ImageResource prevPageDisabledImage();
	
	@Source("org/fedorahosted/flies/webtrans/images/next_page_disabled.png")
	ImageResource nextPageDisabledImage();
	
	@Source("org/fedorahosted/flies/webtrans/images/last_page_disabled.png")
	ImageResource lastPageDisabledImage();
	
	// unused after intro of grey-out icons to reflect disabled state
	@Source("org/fedorahosted/flies/webtrans/images/no_page.png")
	ImageResource noPageImage();
	
	@Source("org/fedorahosted/flies/webtrans/images/approved_unit.png")
	ImageResource approvedUnit();
	
	@Source("org/fedorahosted/flies/webtrans/images/error_unit.png")
	ImageResource errorUnit();
	
	@Source("org/fedorahosted/flies/webtrans/images/fuzzy_unit.png")
	ImageResource fuzzyUnit();
	
	@Source("org/fedorahosted/flies/webtrans/images/new_unit.png")
	ImageResource newUnit();
	
	
}