'use strict';

zanata.createNS('zanata.messages');

zanata.messages = (function ($) {

  var hide = function (el, e) {
    var $el = $(el);
    if (e) e.preventDefault();
    if ($el.hasClass('is-active')) {
      $el.removeClass('is-active');
      setTimeout(function () {
        $el.remove();
      }, 300);
    }
    else {
      $el.addClass('is-removed');
      setTimeout(function () {
        $el.remove();
      }, 300);
    }
  };

  var activate = function (el) {
    $(el).addClass('is-active');
    updatePosition(el);
  };

  var deactivate = function (el) {
    $(el).removeClass('is-active');
  };

  var updatePosition = function (el, elPositionFromTop) {
    var $el = $(el),
        elPosFromTop = '';

    if (typeof elPositionFromTop !== 'undefined') {
      elPosFromTop = elPositionFromTop;
    }
    else if($el.length > 0) {
      elPosFromTop = $el.offset().top;
    }
    else {
      return;
    }

    // Stop negative values setting the position to fixed
    if (elPosFromTop < 0) elPosFromTop = 0;

    if ($(window).scrollTop() > elPosFromTop) {
      $el.addClass('is-fixed');
    } else {
      $el.removeClass('is-fixed');
    }
  };

  var init = function () {

    if ($('.message--global').length > 0) {
      var messageGlobalTop = $('.message--global').offset().top;
    }

    $(document).on('click', '.js-message-remove', function (e) {
      var $el = $(this).parents('.message--removable');
      hide($el, e);
    });

    $(window).scroll(function (){
      updatePosition('.message--global', messageGlobalTop);
    });
  };

  // public API
  return {
    init: init,
    hide: hide,
    activate: activate,
    deactivate: deactivate,
    updatePosition: updatePosition
  };

})(jQuery);

jQuery(function () {
  zanata.messages.init();
});
