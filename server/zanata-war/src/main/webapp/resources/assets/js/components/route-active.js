jQuery(function () {
  'use strict';

  var pathname = window.location.pathname;

  // Check the url, see which links match and make them active
  jQuery('#nav-user a, #nav-main a, #nav-main-side a, #nav-footer a')
    .each(function () {
      var navLink = jQuery(this)
        .attr('href')
        .replace(/\//g, '')
        .replace(/\./g, '');
      if (pathname.toLowerCase().indexOf(navLink) >= 0) {
        jQuery(this).addClass('is-active');
      }
    });

});
