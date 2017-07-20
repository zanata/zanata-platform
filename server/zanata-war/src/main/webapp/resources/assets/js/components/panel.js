'use strict';

zanata.createNS('zanata.panel');

zanata.panel = (function ($) {

  var init = function () {
    var $panelBody = $('.js-panel__body');
    var resizeTimeout;
    function resizePanels() {
      var windowHeight = $(window).height();
      $.each($panelBody, function(i) {
        var $this = $(this);
        var $panel = $this.parents('.js-panel');
        var panelFromTop = $panelBody[i].getBoundingClientRect().top;
        var footerHeight = $('.js-footer').height();
        var panelHeight = Math.floor(
          // Minus 2 to account for rounding errors
          windowHeight - panelFromTop - footerHeight - 2
        );
        $this.css('height', panelHeight);
      });
    }
    if ($panelBody.length > 0) {
      $(window).resize(function(event) {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(resizePanels, 0);
      });
      resizePanels();
    }
  };

  // public API
  return {
    init: init
  };

})(jQuery);

jQuery(function () {
  zanata.panel.init();
});
