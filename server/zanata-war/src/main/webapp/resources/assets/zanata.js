/*! zanata-assets - v0.1.0 - 2017-02-02
* http://zanata.org/
* Copyright (c) 2017 Red Hat; Licensed MIT */
'use strict';

// create the root namespace and making sure we're not overwriting it
var zanata = zanata || {};

zanata.createNS = function (namespace) {
  var nsparts = namespace.split('.');
  var parent = zanata;

  // we want to be able to include or exclude the root namespace so we strip
  // it if it's in the namespace
  if (nsparts[0] === 'zanata') {
    nsparts = nsparts.slice(1);
  }

  // loop through the parts and create a nested namespace if necessary
  for (var i = 0; i < nsparts.length; i++) {
    var partname = nsparts[i];
    // check if the current parent already has the namespace declared
    // if it isn't, then create it
    if (typeof parent[partname] === 'undefined') {
      parent[partname] = {};
    }
    // get a reference to the deepest element in the hierarchy so far
    parent = parent[partname];
  }
  // the parent is now constructed with empty namespaces and can be used.
  // we return the outermost namespace
  return parent;
};

jQuery(function () {
  zanata.tooltip.init('[title]');
});

jQuery(function () {
  'use strict';

  var collapseActiveDropdowns,
    toggleThisCollapseOthers,
    collapseActiveDropdownsOld,
    toggleThisCollapseOthersOld;

  collapseActiveDropdowns = function () {
    jQuery('.js-dropdown.is-active .js-dropdown__toggle').click();
  };

  toggleThisCollapseOthers = function (e) {
    e.preventDefault();
    jQuery(this).blur();
    var $dropdown = jQuery(this).parents('.js-dropdown');
    // $dropdown.removeClass('is-hover');
    jQuery('.js-dropdown.is-active')
      .not($dropdown)
      .removeClass('is-active')
      .parents('.js-dropdown__container')
      .removeClass('is-active');
    $dropdown.toggleClass('is-active')
      .parents('.js-dropdown__container')
      .toggleClass('is-active');
    e.stopPropagation();
  };

  // Add this back when old dropdowns are removed

  // Don't toggle dropdown when clicking links inside it
  jQuery('.js-dropdown__toggle a')
    .bind('click', function (e) {
      e.stopPropagation();
    });

  jQuery(document)
    .bind('click', collapseActiveDropdowns);

  jQuery(document)
    .on('click', '.js-dropdown__toggle', toggleThisCollapseOthers);

});

jQuery(function () {
  'use strict';
  jQuery(document).on('click', '.js-example__setter', function () {
    var exampleState = jQuery(this).attr('data-example');
    // Reset class and apply new one
    jQuery(this).parents('.js-example').find('.js-example__target')
      .attr('class', 'js-example__target').addClass(exampleState);
  });
});

'use strict';

zanata.createNS('zanata.form');

zanata.form = (function ($) {

  var formSearchInProgress = false,
    formSearchInputMouseDown = false;

  // Private methods
  function setCheckRadio ($this) {
    var $input = $this.find('.js-form__checkbox__input,.js-form__radio__input');
    if (!$input.is(':checked')) {
      $input.prop('checked', true).change();
    }
    else if ($input.attr('type') === 'checkbox') {
      $input.prop('checked', false).change();
    }
  }

  function setCheckRadioStatus ($this) {
    var $input = $this.find('.js-form__checkbox__input,.js-form__radio__input'),
      $item = $this.find('.js-form__checkbox__item, .js-form__radio__item');

    // Wait until checkbox/radio change has propagated
    setTimeout(function () {
      if ($input.is(':checked')) {
        $this.addClass('is-checked');
        $item.addClass('is-checked');
      }
      else {
        $this.removeClass('is-checked');
        $item.removeClass('is-checked');
      }
    }, 0);
  }

  function toggleDisableCheckRadio ($this, shouldDisable) {
    var $input = $this.find('.js-form__checkbox__input,.js-form__radio__input');

    setTimeout(function () {
      if (shouldDisable) {
        $input.prop('disabled', true);
        $this.addClass('is-disabled');
      }
      else {
        $input.prop('disabled', false);
        $this.removeClass('is-disabled');
      }
    }, 0);
  }

  function removeRadioStatus ($this) {
    var $input = $this.find('.js-form__radio__input'),
      $item = $this.find('.js-form__checkbox__item, .js-form__radio__item'),
      $radios = jQuery('[name=' + $input.attr('name') + ']')
        .parents('.js-form__radio'),
      $items = $radios.find('.js-form__radio__item');
    setTimeout(function () {
      $radios.removeClass('is-checked');
      $items.removeClass('is-checked');
      if ($input.is(':checked')) {
        $this.addClass('is-checked');
        $item.addClass('is-checked');
      }
    }, 0);
  }

  function updateSearchProgressFlag(e) {
    var $target = $(e.target);
    formSearchInProgress =
      $target.parents('.js-form--search').length > 0 ||
      $target.hasClass('js-form--search');

    if (!formSearchInProgress && !formSearchInputMouseDown) {
      $('.js-form--search__input').blur();
    }
  }

  var appendCheckboxes = function (el, callback) {

    var $elCheckboxes;

    el = el || 'body';
    $elCheckboxes = $(el).find('.js-form__checkbox');

    $.each($elCheckboxes, function () {
      var $this = $(this);

      if (!$this.find('.form__checkbox__item').length) {
        $this
          .append('<span class="form__checkbox__item ' +
            'js-form__checkbox__item"/>');
        setCheckRadioStatus($this);
      }

    });

    if (typeof callback === 'function') {
      callback();
    }

  };

  var appendRadios = function (el, callback) {

    var $elRadios;

    el = el || 'body',
      $elRadios = $(el).find('.js-form__radio');

    $.each($elRadios, function () {
      var $this = $(this);

      if (!$this.find('.form__radio__item').length) {
        $this
          .append('<span class="form__radio__item js-form__radio__item"/>');
        setCheckRadioStatus($this);
      }

    });

    if (typeof callback === 'function') {
      callback();
    }

  };

  var enableInputLoading = function(el, callback) {
    var $el = el ? $(el).find('.js-form__input--load') :
      $('.js-form__input--load'),
      $elParent = $(el).parent(),
      $loader = $('<span />')
        .addClass('js-loader form__loader loader loader--mini');

    // Add a loader if there isn't one
    if (!$elParent.find('.js-loader').length) {
      $el.addClass('form__input--load');
      $elParent.addClass('js-form__load form__load').append($loader);
    }

    if (typeof callback === 'function') {
      callback();
    }
  };

  var activateInputLoading = function(el) {
    enableInputLoading(el, function() {
      var $elParent = $(el).parent(),
        $loader = $elParent.find('.js-loader');

      zanata.loader.activate($loader);
      $elParent.addClass('is-loading');
    });
  };

  var deactivateInputLoading = function(el) {
    enableInputLoading(el, function() {
      var $elParent = $(el).parent(),
        $loader = $elParent.find('.js-loader');

      zanata.loader.deactivate($loader);
      $elParent.removeClass('is-loading');
    });
  };


  // Form Clear

  var clearFormInit = function() {

    $('.js-form__input--clear').addClass('form__input--clear')
      .parent().addClass('form__clear js-form__clear')
      .append('<button class="button--link ' +
        'form__button--clear js-form__button--clear is-hidden">' +
        '<i class="i i--remove"></i></button>');

    clearFormBindings();
  };

  var clearFormBindings = function() {

    $('.js-form__button--clear').on('click', function (e) {
      e.preventDefault();
      $(this).prev('.js-form__input--clear').val('').focus();
      $(this).addClass('is-hidden');
    });

    $('.js-form__input--clear').on('keyup', function () {
      var $this = $(this),
        val = $this.val(),
        $clearButton = $this.next('.js-form__button--clear');

      if (val !== '') {
        $clearButton.removeClass('is-hidden');
      } else {
        $clearButton.addClass('is-hidden');
      }
    });

  };

  var radioBindings = function(el) {
    el = el || 'body';
    $(el).on('click', '.js-form__radio', function (e) {
      setCheckRadio($(this));
      e.preventDefault();
    });

    $(el).on('change', '.js-form__radio__input', function (e) {
      var $parent = $(this).parents('.js-form__radio');
      removeRadioStatus($parent);
      setCheckRadioStatus($parent);
    });

    $(el).on('disable', '.js-form__radio__input', function (e) {
      var $parent = $(this).parents('.js-form__radio');
      toggleDisableCheckRadio($parent, true);
    });

    $(el).on('enable', '.js-form__radio__input', function (e) {
      var $parent = $(this).parents('.js-form__radio');
      toggleDisableCheckRadio($parent, false);
    });
  };

  var checkboxBindings = function(el) {
    el = el || 'body';
    $(el).on('click', '.js-form__checkbox', function (e) {

      if ($(this).hasClass('is-disabled')) {
        return false;
      }

      var directClick = e.target === e.currentTarget;
      var tagName = e.target.tagName.toLowerCase();
      var clickOnButton = tagName === 'button';
      var clickOnAnchor = tagName === 'a';
      var clickOnTextbox = tagName === 'input' && e.target.type === 'text';
      var clickOnSubmit = tagName === 'input' && e.target.type === 'submit';

      var clickOnClickHandler = clickOnButton || clickOnAnchor ||
        clickOnTextbox || clickOnSubmit;

      if (directClick || !clickOnClickHandler) {
        e.preventDefault();
        setCheckRadio($(this));
      }
    });

    $(el).on('change', '.js-form__checkbox__input', function (e) {
      var $parent = $(this).parents('.js-form__checkbox');
      setCheckRadioStatus($parent);
    });

    $(el).on('disable', '.js-form__checkbox__input', function (e) {
      var $parent = $(this).parents('.js-form__checkbox');
      toggleDisableCheckRadio($parent, true);
    });

    $(el).on('enable', '.js-form__checkbox__input', function (e) {
      var $parent = $(this).parents('.js-form__checkbox');
      toggleDisableCheckRadio($parent, false);
    });

  };

  var init = function (el) {

    el = el || 'body';

    appendCheckboxes(el, checkboxBindings(el));
    appendRadios(el, radioBindings(el));
    enableInputLoading();
    clearFormInit(el);

    $('.js-form-password-parent')
      .on('click', '.js-form-password-toggle', function (e) {

        var $passwordInput = $(this)
          .parents('.js-form-password-parent')
          .find('.js-form-password-input');

        e.preventDefault();

        if ($passwordInput.attr('type') === 'password') {
          $passwordInput.attr({
            'type': 'text',
            'autocapitalize': 'off',
            'autocomplete': 'off',
            'autocorrect': 'off',
            'spellcheck': 'false'
          });
          $(this).text('Hide');
        }
        else {
          $passwordInput.attr('type', 'password');
          $(this).text('Show');
        }

        $passwordInput.focus();
      });

    $('.js-form--search__input, .js-form--search__button').on('click',
      function (e) {
        e.stopPropagation();
      }
    );

    $('.js-form--search__input, .js-form--search__button').on('focus',
      function () {
        $(this).parents('.js-form--search').addClass('is-active');
      }
    );

    $('.js-form--search__input, .js-form--search__button').on('blur',
      function (e) {
        if (!formSearchInProgress) {
          $(this).parents('.js-form--search').removeClass('is-active');
        }
      }
    );

    $('.js-form--search').on('mousedown', function(e) {
      formSearchInputMouseDown =
        $(e.target).hasClass('js-form--search__input');
      updateSearchProgressFlag(e);
    });

    $(document).on('mouseup', function(e) {
      updateSearchProgressFlag(e);
      // Reset mouse down
      formSearchInputMouseDown = false;
    });

    $('.js-form__input--copyable')
      .on('mouseup', function () {
        var $this = $(this),
          thisItem = $this[0];
        if (thisItem.selectionStart === thisItem.selectionEnd) {
          $this.select();
        }
      });

  };

  // public API
  return {
    init: init,
    appendCheckboxes: appendCheckboxes,
    appendRadios: appendRadios,
    checkboxBindings: checkboxBindings,
    radioBindings: radioBindings,
    activateInputLoading: activateInputLoading,
    deactivateInputLoading: deactivateInputLoading
  };

})(jQuery);

jQuery(function () {
  zanata.form.init();
});

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

'use strict';

zanata.createNS('zanata.modal');

zanata.modal = (function ($) {

  var show = function (el) {
    var $el = $(el);
    $el.addClass('is-active').scrollTop(0);
    $('body').addClass('is-modal').css('padding-right', getScrollBarWidth());
  };

  var hide = function (el) {
    var $el = $(el);
    $el.removeClass('is-active');
    $('body').removeClass('is-modal').removeAttr('style');
  };

  var init = function () {

    $(document).on('click', '[data-toggle="modal"]', function () {
      var modalTarget = $(this).attr('data-target');
      $(modalTarget).trigger('show.zanata.modal');
    });

    $(document).on('click', '.is-modal', function (e) {
      if ($(e.target).not('.modal__dialog') &&
        !$(e.target).parents('.modal__dialog').length) {
        $('.modal.is-active').trigger('hide.zanata.modal');
      }
    });

    $(document).on('keyup', function (e) {
      if (e.keyCode === 27) {
        e.stopPropagation();
        $('.modal.is-active').trigger('hide.zanata.modal');
      }
    });

    $(document).on('click', '[data-dismiss="modal"]', function () {
      $(this).parents('.modal.is-active').trigger('hide.zanata.modal');
    });

    $(document).on('hide.zanata.modal', function (e) {
      hide(e.target);
    });

    $(document).on('show.zanata.modal', function (e) {
      show(e.target);
    });

  };

  function getScrollBarWidth () {
    var $outer = $('<div>').css({visibility: 'hidden', width: 100,
        overflow: 'scroll'}).appendTo('body'),
      widthWithScroll = $('<div>').css({width: '100%'})
        .appendTo($outer).outerWidth();
    $outer.remove();
    return 100 - widthWithScroll;
  }

  // public API
  return {
    init: init,
    show: show,
    hide: hide
  };

})(jQuery);

jQuery(function () {
  zanata.modal.init();
});

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

(function ($) {
  'use strict';
  $(document).on('click', '.js-reveal__show', function () {
    var $revealTarget = $($(this).attr('data-target')),
      $revealTargetInput = $revealTarget.find('.js-reveal__target__input'),
      $revealParent = $(this).parents('.js-reveal');
    $(this).addClass('is-hidden');
    $revealParent.addClass('is-active');
    $revealTarget.toggleClass('is-active');
    setTimeout(function () {
      $revealTargetInput.focus();
    }, 100);
  });
  $(document).on('click', '.js-reveal__toggle', function (e) {
    var $revealTarget = $($(this).attr('data-target')),
      $revealTargetInput = $revealTarget.find('.js-reveal__target__input'),
      $revealParent = $(this).parents('.js-reveal'),
      $revealText = $(this).find('.js-reveal__toggle__text'),
      revealTextValue = $revealText.text(),
      revealToggleValue = $revealText.attr('data-toggle-value'),
      revealTitle = $(this).attr('title') ||
        $(this).attr('data-original-title'),
      revealToggleTitle = $(this).attr('data-toggle-title');

    // Label need to register the click so it applies to the checkbox or radio
    // it is attached to
    if (!$(e.target).is('label')) {
      e.preventDefault();
    }
    $(this).toggleClass('is-active');
    $revealParent.toggleClass('is-active');
    $revealTarget.toggleClass('is-active is-hidden');
    if (revealToggleTitle && revealTitle) {
      $(this).attr('data-toggle-title', revealTitle);
      zanata.tooltip.refresh($(this), revealToggleTitle);
    }
    if (revealTextValue && revealToggleValue) {
      $revealText.text(revealToggleValue);
      $revealText.attr('data-toggle-value', revealTextValue);
    }
    setTimeout(function () {
      $revealTargetInput.focus();
    }, 100);
  });

  $(document).on('click', '.js-reveal__reset', function () {
    var $revealTarget = $($(this).attr('data-target')),
      $revealTargetInput = $revealTarget.find('.js-reveal__target__input');
    $revealTargetInput.val('').focus();
    $(this).addClass('is-hidden');
  });
  $(document).on('click', '.js-reveal__cancel', function () {
    var $revealTarget = $($(this).attr('data-target')),
      $revealTargetInput = $revealTarget.find('.js-reveal__target__input'),
      $revealParent = $(this).parents('.js-reveal');
    $revealTarget.removeClass('is-active');
    $revealTargetInput.blur();
    $revealTargetInput.val('');
    $revealParent.find('.js-reveal__reset').addClass('is-hidden');
    $revealParent.find('.js-reveal__show').removeClass('is-hidden').focus();
  });
  $(document).on('keyup', '.js-reveal__target__input', function (e) {
    var $revealParent = $(this).parents('.js-reveal'),
      $revealReset = $revealParent.find('.js-reveal__reset'),
      $revealCancel = $revealParent.find('.js-reveal__cancel');
    if ($(this).val() !== '') {
      $revealReset.removeClass('is-hidden');
    }
    else {
      $revealReset.addClass('is-hidden');
    }
    if (e.keyCode === 27) {
      $revealCancel.click();
    }
  });

})(jQuery);

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
