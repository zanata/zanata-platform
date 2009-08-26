/**
 *
 * Copyright (c) 2007 Tom Deater (http://www.tomdeater.com)
 * Licensed under the MIT License:
 * http://www.opensource.org/licenses/mit-license.php
 * 
 */
 
(function($) {
	/**
	 * equalizes the heights of all elements in a jQuery collection
	 * thanks to John Resig for optimizing this!
	 * usage: $("#col1, #col2, #col3").equalizeCols();
	 */
	 
	$.fn.equalizeCols = function(){
		var height = 0,
			reset = $.browser.msie ? "1%" : "auto";
  
		return this
			.css("height", reset)
			.each(function() {
				height = Math.max(height, this.offsetHeight);
			})
			.css("height", height)
			.each(function() {
				var h = this.offsetHeight;
				if (h > height) {
					$(this).css("height", height - (h - height));
				};
			});
			
	};
	
})(jQuery);

(function($) {
	/**
	 * equalizes the heights of all elements in a jQuery collection
	 * thanks to John Resig for optimizing this!
	 * usage: $("#col1, #col2, #col3").equalizeCols();
	 */
	 
	$.fn.equalizeWidths = function(){
		var width = 0,
			reset = $.browser.msie ? "1%" : "auto";
  
		return this
			.css("width", reset)
			.each(function() {
				width = Math.max(width, this.offsetWidth);
			})
			.css("width", width)
			.each(function() {
				var w = this.offsetWidth;
				if (w > width) {
					$(this).css("width", width - (w - width));
				};
			});
			
	};
	
})(jQuery);