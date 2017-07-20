'use strict';

zanata.createNS('zanata.loader');

zanata.loader = (function ($) {

  var activate = function (el) {
    var $el = $(el),
        $label = $el.find('.loader__label');

    if ($label.length > 0) {
      $label.append('<span class="loader__spinner"><span></span>' +
        '<span></span><span></span></span>');
    }
    else {
      $el.append('<span class="loader__spinner"><span></span>' +
        '<span></span><span></span></span>');
    }

    $el.addClass('is-active');
  };

  var deactivate = function (el) {
    var $el = $(el);

    $el.find('.loader__spinner').remove();
    $el.removeClass('is-active');
  };

  var init = function () {

    $(document).on('click', '.js-loader, .loader', function (e) {
      // If it is not active
      e.preventDefault();
      if (!$(this).hasClass('is-active')) {
        activate(this);
      }
    });

  };

  // public API
  return {
    init: init,
    activate: activate,
    deactivate: deactivate
  };

})(jQuery);

jQuery(function () {
  zanata.loader.init();
});

