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
