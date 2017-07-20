'use strict';

zanata.createNS('zanata.tabs');

zanata.tabs = (function ($) {

  var activate = function (el) {

    var $this = $(el),
        targetHash = $this.attr('href'),
        targetID = targetHash.replace('#', ''),
        $parent = $this.closest('.js-tabs');
    // data-content attribute should have a selector for the
    // content container for the tab
    if($this.is('[data-content]')) {
      targetHash = $this.attr('data-content');
    }
    if (!$this.parent().hasClass('is-active')) {
      // Remove all is-active classes
      $parent
        .find('> .js-tabs-content > li, > .js-tabs-nav > li > a')
        .removeClass('is-active');
      // Add relevant is-active classes
      $this.blur().addClass('is-active');
      // Add hashed class so we can remove ID to change the hash
      $(targetHash)
        .addClass('is-active');
      // When changing tabs check for panels and resize to fit browser
      if ($(targetHash).find('.js-panel__body').length > 0) {
        zanata.panel.init();
      }
    }

  };

  var init = function () {

    $('.js-tabs').on('click', '.js-tabs-nav > li > a', function (e) {
      e.preventDefault();
      activate(this);
    });

  };

  // public API
  return {
    init: init,
    activate: activate
  };

})(jQuery);

jQuery(function () {
  zanata.tabs.init();
});
