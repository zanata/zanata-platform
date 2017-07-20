'use strict';

zanata.createNS('zanata.tooltip');

zanata.tooltip = (function ($) {

  // Private methods
  var init = function (el) {
    $(el).tooltip({
      placement: 'auto bottom',
      container: 'body',
      delay: {
        show: '500',
        hide: '100'
      }
    });
  };

  var refresh = function (el, newTitle) {
    $(el)
      .tooltip('hide')
      .attr('data-original-title', newTitle)
      .tooltip('fixTitle')
      .tooltip('show');
  };

  // public API
  return {
    init: init,
    refresh: refresh
  };

})(jQuery);
